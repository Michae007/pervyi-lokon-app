// Конфигурация
const CONFIG = {
    SCRIPT_URL: 'ВАШ_URL_СКРИПТА', // Замените на ваш URL из Google Apps Script
    
    // График работы
    WORK_SCHEDULE: {
        1: { start: '10:00', end: '19:00', working: true }, // понедельник
        2: { start: '10:00', end: '19:00', working: true }, // вторник
        3: { start: '10:00', end: '19:00', working: true }, // среда
        4: { start: '10:00', end: '19:00', working: false }, // четверг - выходной
        5: { start: '10:00', end: '19:00', working: true }, // пятница
        6: { start: '10:00', end: '16:00', working: true }, // суббота
        0: { start: '10:00', end: '16:00', working: false }  // воскресенье - выходной
    }
};

// Инициализация при загрузке страницы
document.addEventListener('DOMContentLoaded', function() {
    initializeDatePicker();
    initializeServiceSelection();
    initializeTimeSlots();
    setupFormValidation();
});

// Установка минимальной даты и валидация выходных
function initializeDatePicker() {
    const dateInput = document.getElementById('date');
    const today = new Date().toISOString().split('T')[0];
    dateInput.min = today;
    dateInput.value = today;
    
    dateInput.addEventListener('change', function() {
        validateSelectedDate();
        updateTimeSlots();
    });
}

// Валидация выбранной даты
function validateSelectedDate() {
    const dateInput = document.getElementById('date');
    const selectedDate = new Date(dateInput.value);
    const dayOfWeek = selectedDate.getDay();
    const schedule = CONFIG.WORK_SCHEDULE[dayOfWeek];
    
    if (!schedule || !schedule.working) {
        showError('В этот день парикмахерская не работает. Пожалуйста, выберите другой день.');
        document.getElementById('time').innerHTML = '<option value="">Выходной день</option>';
        return false;
    }
    
    hideError();
    return true;
}

// Инициализация выбора услуги
function initializeServiceSelection() {
    const serviceCards = document.querySelectorAll('.service-card');
    serviceCards.forEach(card => {
        card.addEventListener('click', function() {
            // Снимаем выделение со всех карточек
            serviceCards.forEach(c => c.classList.remove('selected'));
            // Выделяем выбранную
            this.classList.add('selected');
            
            // Устанавливаем значения в скрытые поля
            document.getElementById('service').value = this.dataset.service;
            document.getElementById('price').value = this.dataset.price;
        });
    });
}

// Обновление доступных временных слотов
function updateTimeSlots() {
    const date = document.getElementById('date').value;
    if (!date) return;
    
    const dayOfWeek = new Date(date).getDay();
    const schedule = CONFIG.WORK_SCHEDULE[dayOfWeek];
    const timeSelect = document.getElementById('time');
    
    timeSelect.innerHTML = '<option value="">Загружаем доступное время...</option>';
    timeSelect.disabled = true;
    
    if (!schedule || !schedule.working) {
        timeSelect.innerHTML = '<option value="">Выходной день</option>';
        return;
    }
    
    // Генерируем временные слоты
    setTimeout(() => {
        const slots = generateTimeSlots(schedule.start, schedule.end, 30);
        timeSelect.innerHTML = '<option value="">Выберите время</option>';
        
        slots.forEach(slot => {
            const option = document.createElement('option');
            option.value = slot;
            option.textContent = slot;
            timeSelect.appendChild(option);
        });
        
        timeSelect.disabled = false;
    }, 500);
}

// Генерация временных слотов
function generateTimeSlots(startTime, endTime, intervalMinutes) {
    const slots = [];
    const [startHour, startMinute] = startTime.split(':').map(Number);
    const [endHour, endMinute] = endTime.split(':').map(Number);
    
    let currentHour = startHour;
    let currentMinute = startMinute;
    
    while (currentHour < endHour || (currentHour === endHour && currentMinute < endMinute)) {
        const timeString = `${currentHour.toString().padStart(2, '0')}:${currentMinute.toString().padStart(2, '0')}`;
        slots.push(timeString);
        
        currentMinute += intervalMinutes;
        if (currentMinute >= 60) {
            currentHour += 1;
            currentMinute = 0;
        }
    }
    
    return slots;
}

function initializeTimeSlots() {
    updateTimeSlots();
}

// Настройка валидации формы
function setupFormValidation() {
    const phoneInput = document.getElementById('phone');
    
    phoneInput.addEventListener('input', function(e) {
        let value = e.target.value.replace(/\D/g, '');
        if (value.startsWith('7')) {
            value = '+' + value;
        } else if (value.startsWith('8')) {
            value = '+7' + value.slice(1);
        } else if (!value.startsWith('+')) {
            value = '+7' + value;
        }
        e.target.value = value;
    });
}

// Показать сообщение об ошибке
function showError(message) {
    const errorAlert = document.getElementById('errorAlert');
    document.getElementById('errorMessage').textContent = message;
    errorAlert.style.display = 'flex';
    
    // Автоматически скрыть через 10 секунд
    setTimeout(() => {
        errorAlert.style.display = 'none';
    }, 10000);
}

// Скрыть сообщение об ошибке
function hideError() {
    document.getElementById('errorAlert').style.display = 'none';
}

// Показать сообщение об успехе
function showSuccess() {
    const successAlert = document.getElementById('successAlert');
    successAlert.style.display = 'flex';
    
    // Автоматически скрыть через 10 секунд
    setTimeout(() => {
        successAlert.style.display = 'none';
    }, 10000);
}

// Скрыть все уведомления
function hideAllAlerts() {
    document.getElementById('successAlert').style.display = 'none';
    document.getElementById('errorAlert').style.display = 'none';
}

// Отправка формы
document.getElementById('appointmentForm').addEventListener('submit', async function(e) {
    e.preventDefault();
    
    const submitBtn = document.getElementById('submitBtn');
    const btnText = submitBtn.querySelector('.btn-text');
    const btnLoading = submitBtn.querySelector('.btn-loading');
    
    // Скрыть предыдущие уведомления
    hideAllAlerts();
    
    // Валидация выбора услуги
    if (!document.getElementById('service').value) {
        showError('Пожалуйста, выберите услугу');
        return;
    }
    
    // Валидация даты
    if (!validateSelectedDate()) {
        return;
    }
    
    // Показать загрузку
    submitBtn.disabled = true;
    btnText.style.display = 'none';
    btnLoading.style.display = 'flex';
    
    const formData = {
        clientName: document.getElementById('clientName').value.trim(),
        phone: document.getElementById('phone').value.trim(),
        childAge: document.getElementById('childAge').value,
        service: document.getElementById('service').value,
        price: document.getElementById('price').value,
        date: document.getElementById('date').value,
        time: document.getElementById('time').value,
        duration: 30,
        notes: document.getElementById('notes').value.trim()
    };
    
    try {
        const response = await fetch(CONFIG.SCRIPT_URL, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                action: 'create',
                data: formData
            })
        });
        
        const result = await response.json();
        
        if (result.success) {
            showSuccess();
            
            // Сбрасываем форму
            document.getElementById('appointmentForm').reset();
            document.querySelectorAll('.service-card').forEach(card => {
                card.classList.remove('selected');
            });
            
            // Обновляем доступное время
            initializeTimeSlots();
            
            // Прокручиваем к верху
            window.scrollTo({ top: 0, behavior: 'smooth' });
            
        } else {
            throw new Error(result.error || 'Неизвестная ошибка');
        }
    } catch (error) {
        console.error('Error:', error);
        showError(error.message || 'Ошибка при создании записи. Пожалуйста, попробуйте еще раз или позвоните нам.');
    } finally {
        // Восстанавливаем кнопку
        submitBtn.disabled = false;
        btnText.style.display = 'block';
        btnLoading.style.display = 'none';
    }
});

// Инициализация времени при загрузке
initializeTimeSlots();
