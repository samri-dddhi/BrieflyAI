document.addEventListener('DOMContentLoaded', () => {
    const notesEl = document.getElementById('notes');
    const summarizeBtn = document.getElementById('summarizeBtn');
    const saveNotesBtn = document.getElementById('saveNotesBtn');
    const resultEl = document.getElementById('result');

    // Check if all elements exist before using them
    if (!notesEl || !summarizeBtn || !saveNotesBtn || !resultEl) {
        console.error('Required DOM elements not found.');
        return;
    }

    // Load saved notes
    chrome.storage.local.get(['researchNotes'], function (result) {
        if (result.researchNotes) {
            notesEl.value = result.researchNotes;
        }
    });

    // Event handlers
    summarizeBtn.addEventListener('click', summarizeText);
    saveNotesBtn.addEventListener('click', () => {
        const notes = notesEl.value;
        chrome.storage.local.set({ researchNotes: notes }, () => {
            alert('Notes saved successfully!');
        });
    });

    // Function to show the result
    function showResult(content) {
        resultEl.innerHTML = `
            <div class="result-item">
                <div class="result-content">${content}</div>
            </div>`;
    }

    // Summarize selected text
    async function summarizeText() {
        try {
            const [tab] = await chrome.tabs.query({ active: true, currentWindow: true });
            const [{ result }] = await chrome.scripting.executeScript({
                target: { tabId: tab.id },
                func: () => window.getSelection().toString(),
            });

            if (!result || result.trim() === '') {
                showResult('No text selected');
                return;
            }

            const response = await fetch('http://localhost:8080/api/research/process', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ content: result, operation: 'summarize' }),
            });

            if (!response.ok) {
                throw new Error(`API Error: ${response.status}`);
            }

            const text = await response.text();
            showResult(text.replace(/\n/g, '<br>'));
        } catch (error) {
            showResult('Error: ' + error.message);
        }
    }
});
