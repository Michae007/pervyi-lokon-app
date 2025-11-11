// Конфигурация
const CONFIG = {
  SHEET_ID: 'YOUR_SHEET_ID_HERE',
  SHEET_NAME: 'Appointments',
  TELEGRAM_BOT_TOKEN: 'YOUR_BOT_TOKEN',
  TELEGRAM_CHAT_ID: 'YOUR_CHAT_ID'
};

// Главная функция обработки запросов
function doPost(e) {
  try {
    const data = JSON.parse(e.postData.contents);
    const action = data.action;
    
    let response;
    
    switch(action) {
      case 'create':
        response = createAppointment(data.data);
        break;
      case 'getAppointments':
        response = getAppointments(data.date);
        break;
      case 'updateStatus':
        response = updateAppointmentStatus(data.data);
        break;
      case 'updateMaster':
        response = updateAppointmentMaster(data.data);
        break;
      default:
        throw new Error('Unknown action: ' + action);
    }
    
    return ContentService.createTextOutput(JSON.stringify(response))
      .setMimeType(ContentService.MimeType.JSON);
      
  } catch (error) {
    return ContentService.createTextOutput(JSON.stringify({
      success: false,
      error: error.message
    })).setMimeType(ContentService.MimeType.JSON);
  }
}

// Функция для GET запросов (для тестирования)
function doGet(e) {
  const action = e.parameter.action;
  const date = e.parameter.date;
  
  if (action === 'getAppointments' && date) {
    const response = getAppointments(date);
    return ContentService.createTextOutput(JSON.stringify(response))
      .setMimeType(ContentService.MimeType.JSON);
  }
  
  return ContentService.createTextOutput(JSON.stringify({
    success: false,
    error: 'Invalid parameters'
  })).setMimeType(ContentService.MimeType.JSON);
}

// Создание новой записи
function createAppointment(appointmentData) {
  const sheet = getSheet();
  const id = Utilities.getUuid();
  const timestamp = new Date();
  
  const row = [
    id,
    timestamp,
    appointmentData.clientName,
    appointmentData.phone,
    appointmentData.childAge,
    appointmentData.service,
    appointmentData.price,
    appointmentData.date,
    appointmentData.time,
    appointmentData.duration || '30',
    '', // master
    'новая', // status
    appointmentData.notes,
    appointmentData.source || 'website'
  ];
  
  sheet.appendRow(row);
  
  // Отправка уведомления в Telegram
  sendTelegramNotification(`Новая запись: ${appointmentData.clientName}, ${appointmentData.date} ${appointmentData.time}`);
  
  return {
    success: true,
    id: id
  };
}

// Получение записей на дату
function getAppointments(date) {
  const sheet = getSheet();
  const data = sheet.getDataRange().getValues();
  const headers = data[0];
  
  const appointments = [];
  
  for (let i = 1; i < data.length; i++) {
    const row = data[i];
    if (row[7] === date) { // date column
      appointments.push({
        id: row[0],
        timestamp: row[1],
        clientName: row[2],
        phone: row[3],
        childAge: row[4],
        service: row[5],
        price: row[6],
        date: row[7],
        time: row[8],
        duration: row[9],
        master: row[10],
        status: row[11],
        notes: row[12],
        source: row[13]
      });
    }
  }
  
  return {
    success: true,
    appointments: appointments
  };
}

// Обновление статуса
function updateAppointmentStatus(updateData) {
  const sheet = getSheet();
  const data = sheet.getDataRange().getValues();
  
  for (let i = 1; i < data.length; i++) {
    if (data[i][0] === updateData.id) {
      sheet.getRange(i + 1, 12).setValue(updateData.status); // status column
      
      sendTelegramNotification(`Статус изменен: ${updateData.id} -> ${updateData.status}`);
      return { success: true };
    }
  }
  
  throw new Error('Appointment not found');
}

// Вспомогательные функции
function getSheet() {
  const spreadsheet = SpreadsheetApp.openById(CONFIG.SHEET_ID);
  return spreadsheet.getSheetByName(CONFIG.SHEET_NAME);
}

function sendTelegramNotification(message) {
  if (!CONFIG.TELEGRAM_BOT_TOKEN) return;
  
  const url = `https://api.telegram.org/bot${CONFIG.TELEGRAM_BOT_TOKEN}/sendMessage`;
  const payload = {
    chat_id: CONFIG.TELEGRAM_CHAT_ID,
    text: message,
    parse_mode: 'HTML'
  };
  
  const options = {
    method: 'post',
    contentType: 'application/json',
    payload: JSON.stringify(payload)
  };
  
  UrlFetchApp.fetch(url, options);
}
