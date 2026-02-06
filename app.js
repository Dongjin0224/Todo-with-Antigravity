// ===== API 설정 =====
const API_BASE_URL = 'http://localhost:8080/api/todos';

// ===== DOM Elements =====
const todoInput = document.getElementById('todoInput');
const addBtn = document.getElementById('addBtn');
const todoList = document.getElementById('todoList');
const emptyState = document.getElementById('emptyState');
const cardFooter = document.getElementById('cardFooter');
const itemsLeft = document.getElementById('itemsLeft');
const clearCompleted = document.getElementById('clearCompleted');
const filterBtns = document.querySelectorAll('.filter-btn');
const dateDisplay = document.getElementById('dateDisplay');
const themeToggle = document.getElementById('themeToggle');
const loadingOverlay = document.getElementById('loadingOverlay');

// Count elements
const allCount = document.getElementById('allCount');
const activeCount = document.getElementById('activeCount');
const completedCount = document.getElementById('completedCount');

// ===== State =====
let todos = [];
let currentFilter = 'all';
let draggedItem = null;

// ===== Initialize =====
async function init() {
    initTheme();
    displayDate();
    await fetchTodos();
    setupEventListeners();
}

// ===== Theme Handling =====
function initTheme() {
    const savedTheme = localStorage.getItem('theme') || 'dark';
    document.documentElement.setAttribute('data-theme', savedTheme);
    updateThemeIcon(savedTheme);
}

function toggleTheme() {
    const currentTheme = document.documentElement.getAttribute('data-theme');
    const newTheme = currentTheme === 'light' ? 'dark' : 'light';

    document.documentElement.setAttribute('data-theme', newTheme);
    localStorage.setItem('theme', newTheme);
    updateThemeIcon(newTheme);
}

function updateThemeIcon(theme) {
    const icon = themeToggle.querySelector('.theme-icon');
    icon.textContent = theme === 'light' ? '☀️' : '🌙';
}

// ===== Loading State =====
function setLoading(isLoading) {
    if (isLoading) {
        loadingOverlay.classList.add('show');
    } else {
        loadingOverlay.classList.remove('show');
    }
}

// ===== Display Current Date =====
function displayDate() {
    const options = {
        year: 'numeric',
        month: 'long',
        day: 'numeric',
        weekday: 'long'
    };
    const today = new Date().toLocaleDateString('ko-KR', options);
    dateDisplay.textContent = today;
}

// ===== API 호출 함수들 =====

/**
 * 서버에서 Todo 목록 가져오기
 */
async function fetchTodos() {
    setLoading(true);
    try {
        const response = await fetch(`${API_BASE_URL}?filter=${currentFilter}`);
        if (!response.ok) throw new Error('Failed to fetch todos');
        todos = await response.json();
        renderTodos();
    } catch (error) {
        console.error('Error fetching todos:', error);
        showError('서버와 연결할 수 없습니다. 백엔드가 실행 중인지 확인하세요.');
    } finally {
        setLoading(false);
    }
}

/**
 * 새 Todo 서버에 저장
 */
async function createTodo(text) {
    setLoading(true);
    try {
        const response = await fetch(API_BASE_URL, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ text: text })
        });

        if (!response.ok) {
            const error = await response.json();
            throw new Error(error.message || 'Failed to create todo');
        }

        const newTodo = await response.json();
        todos.unshift(newTodo);
        renderTodos();
        return newTodo;
    } catch (error) {
        console.error('Error creating todo:', error);
        showError('할 일 추가에 실패했습니다.');
        throw error;
    } finally {
        setLoading(false);
    }
}

/**
 * Todo 완료 상태 토글
 */
async function toggleTodoOnServer(id) {
    try {
        const response = await fetch(`${API_BASE_URL}/${id}/toggle`, {
            method: 'PATCH'
        });

        if (!response.ok) throw new Error('Failed to toggle todo');

        const updated = await response.json();
        todos = todos.map(todo => todo.id === id ? updated : todo);
        renderTodos();
    } catch (error) {
        console.error('Error toggling todo:', error);
        showError('상태 변경에 실패했습니다.');
    }
}

/**
 * Todo 수정
 */
async function updateTodoOnServer(id, text) {
    try {
        const response = await fetch(`${API_BASE_URL}/${id}`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ text: text })
        });

        if (!response.ok) throw new Error('Failed to update todo');

        const updated = await response.json();
        todos = todos.map(todo => todo.id === id ? updated : todo);
        renderTodos();
    } catch (error) {
        console.error('Error updating todo:', error);
        showError('수정에 실패했습니다.');
    }
}

/**
 * Todo 삭제
 */
async function deleteTodoOnServer(id) {
    try {
        const response = await fetch(`${API_BASE_URL}/${id}`, {
            method: 'DELETE'
        });

        if (!response.ok) throw new Error('Failed to delete todo');

        todos = todos.filter(todo => todo.id !== id);
        renderTodos();
    } catch (error) {
        console.error('Error deleting todo:', error);
        showError('삭제에 실패했습니다.');
    }
}

/**
 * 완료된 Todo 일괄 삭제
 */
async function clearCompletedOnServer() {
    try {
        const response = await fetch(`${API_BASE_URL}/completed`, {
            method: 'DELETE'
        });

        if (!response.ok) throw new Error('Failed to clear completed');

        todos = todos.filter(todo => !todo.completed);
        renderTodos();
    } catch (error) {
        console.error('Error clearing completed:', error);
        showError('완료 항목 삭제에 실패했습니다.');
    }
}

// ===== Event Listeners =====
function setupEventListeners() {
    // Add todo
    addBtn.addEventListener('click', addTodo);
    todoInput.addEventListener('keypress', (e) => {
        if (e.key === 'Enter') addTodo();
    });

    // Filter buttons
    filterBtns.forEach(btn => {
        btn.addEventListener('click', async () => {
            filterBtns.forEach(b => b.classList.remove('active'));
            btn.classList.add('active');
            currentFilter = btn.dataset.filter;
            await fetchTodos();  // 서버에서 필터링된 데이터 가져오기
        });
    });

    // Clear completed
    clearCompleted.addEventListener('click', clearCompletedOnServer);

    // Theme toggle
    themeToggle.addEventListener('click', toggleTheme);
}

// ===== Add Todo =====
async function addTodo() {
    const text = todoInput.value.trim();
    if (!text) {
        shakeInput();
        return;
    }

    try {
        await createTodo(text);
        todoInput.value = '';
        todoInput.focus();
    } catch (error) {
        // 에러는 createTodo에서 처리됨
    }
}

// ===== Show Error Message =====
function showError(message) {
    const existing = document.querySelector('.error-toast');
    if (existing) existing.remove();

    const toast = document.createElement('div');
    toast.className = 'error-toast';
    toast.innerHTML = `<span style="font-size: 18px">⚠️</span> <span>${message}</span>`;

    document.body.appendChild(toast);

    setTimeout(() => {
        toast.style.opacity = '0';
        toast.style.transform = 'translateY(-20px)';
        setTimeout(() => toast.remove(), 300);
    }, 3000);
}

// ===== Shake Input Animation =====
function shakeInput() {
    todoInput.style.animation = 'shake 0.5s ease';
    setTimeout(() => {
        todoInput.style.animation = '';
    }, 500);
}

// Add shake keyframes dynamically
const shakeStyles = document.createElement('style');
shakeStyles.textContent = `
    @keyframes shake {
        0%, 100% { transform: translateX(0); }
        25% { transform: translateX(-10px); }
        75% { transform: translateX(10px); }
    }
`;
document.head.appendChild(shakeStyles);

// ===== Render Todos =====
function renderTodos() {
    todoList.innerHTML = '';

    if (todos.length === 0) {
        emptyState.classList.add('show');
        cardFooter.classList.add('hidden');
    } else {
        emptyState.classList.remove('show');
        cardFooter.classList.remove('hidden');

        todos.forEach(todo => {
            const li = createTodoElement(todo);
            todoList.appendChild(li);
        });
    }

    updateCounts();
}

// ===== Create Todo Element =====
function createTodoElement(todo) {
    const li = document.createElement('li');
    li.className = `todo-item ${todo.completed ? 'completed' : ''}`;
    li.dataset.id = todo.id;
    li.draggable = true;

    li.innerHTML = `
        <label class="checkbox-wrapper">
            <input type="checkbox" ${todo.completed ? 'checked' : ''}>
            <span class="checkmark"></span>
        </label>
        <span class="todo-text">${escapeHtml(todo.text)}</span>
        <button class="delete-btn" aria-label="삭제">×</button>
    `;

    // Checkbox event - 서버에 상태 변경 요청
    const checkbox = li.querySelector('input[type="checkbox"]');
    checkbox.addEventListener('change', () => toggleTodoOnServer(todo.id));

    // Delete button event - 서버에 삭제 요청
    const deleteBtn = li.querySelector('.delete-btn');
    deleteBtn.addEventListener('click', () => {
        li.style.animation = 'slideOut 0.3s ease forwards';
        setTimeout(() => deleteTodoOnServer(todo.id), 300);
    });

    // Double click to edit
    const textSpan = li.querySelector('.todo-text');
    textSpan.addEventListener('dblclick', () => editTodo(li, todo));

    // Drag events
    li.addEventListener('dragstart', handleDragStart);
    li.addEventListener('dragend', handleDragEnd);
    li.addEventListener('dragover', handleDragOver);
    li.addEventListener('drop', handleDrop);

    return li;
}

// ===== Escape HTML =====
function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

// Add slideOut keyframes
const slideOutStyles = document.createElement('style');
slideOutStyles.textContent = `
    @keyframes slideOut {
        to {
            opacity: 0;
            transform: translateX(100px);
        }
    }
`;
document.head.appendChild(slideOutStyles);

// ===== Edit Todo =====
function editTodo(li, todo) {
    const textSpan = li.querySelector('.todo-text');
    const originalText = todo.text;

    textSpan.contentEditable = true;
    textSpan.classList.add('editing');
    textSpan.focus();

    // Select all text
    const range = document.createRange();
    range.selectNodeContents(textSpan);
    const selection = window.getSelection();
    selection.removeAllRanges();
    selection.addRange(range);

    const finishEdit = async () => {
        textSpan.contentEditable = false;
        textSpan.classList.remove('editing');

        const newText = textSpan.textContent.trim();
        if (newText && newText !== originalText) {
            await updateTodoOnServer(todo.id, newText);
        } else {
            textSpan.textContent = originalText;
        }
    };

    textSpan.addEventListener('blur', finishEdit, { once: true });
    textSpan.addEventListener('keypress', (e) => {
        if (e.key === 'Enter') {
            e.preventDefault();
            textSpan.blur();
        }
    });

    textSpan.addEventListener('keydown', (e) => {
        if (e.key === 'Escape') {
            textSpan.textContent = originalText;
            textSpan.blur();
        }
    });
}

// ===== Drag & Drop =====
function handleDragStart(e) {
    draggedItem = this;
    this.classList.add('dragging');
    e.dataTransfer.effectAllowed = 'move';
}

function handleDragEnd() {
    this.classList.remove('dragging');
    draggedItem = null;
}

function handleDragOver(e) {
    e.preventDefault();
    e.dataTransfer.dropEffect = 'move';
}

async function handleDrop(e) {
    e.preventDefault();

    if (this === draggedItem) return;

    const draggedId = parseInt(draggedItem.dataset.id);
    const targetId = parseInt(this.dataset.id);

    const draggedIndex = todos.findIndex(t => t.id === draggedId);
    const targetIndex = todos.findIndex(t => t.id === targetId);

    // Optimistic UI Update
    const [removed] = todos.splice(draggedIndex, 1);
    todos.splice(targetIndex, 0, removed);
    renderTodos();

    // Sync with Server
    // 순서가 변경된 모든 아이템의 displayOrder 업데이트
    // 실제로는 효율성을 위해 변경된 범위만 업데이트하거나, 
    // LinkedList 처럼 앞뒤/순서값 등을 조정하는 방식이 좋음.
    // 여기서는 간단히 전체 리스트 순서를 재할당하여 전송 (데이터 양이 적으므로)

    try {
        const updatePromises = todos.map((todo, index) => {
            // 순서가 바뀐 항목만 요청
            if (todo.displayOrder !== index) {
                todo.displayOrder = index;
                return fetch(`${API_BASE_URL}/${todo.id}`, {
                    method: 'PUT',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ displayOrder: index })
                });
            }
            return Promise.resolve();
        });

        await Promise.all(updatePromises);
    } catch (error) {
        console.error('Error syncing order:', error);
        showError('순서 저장에 실패했습니다.');
        // 에러 시 원래대로 되돌리는 로직이 필요할 수 있음
    }
}

// ===== Update Counts =====
async function updateCounts() {
    try {
        const response = await fetch(`${API_BASE_URL}/stats`);
        if (response.ok) {
            const stats = await response.json();
            itemsLeft.textContent = stats.active;
            allCount.textContent = stats.total;
            activeCount.textContent = stats.active;
            completedCount.textContent = stats.completed;
        }
    } catch (error) {
        // 로컬 데이터로 폴백
        const active = todos.filter(t => !t.completed).length;
        const completed = todos.filter(t => t.completed).length;
        itemsLeft.textContent = active;
        allCount.textContent = todos.length;
        activeCount.textContent = active;
        completedCount.textContent = completed;
    }
}

// ===== Start App =====
init();
