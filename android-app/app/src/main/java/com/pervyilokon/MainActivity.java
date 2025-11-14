package com.pervyilokon;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.pervyilokon.adapters.AppointmentAdapter;
import com.pervyilokon.api.ApiClient;
import com.pervyilokon.api.GoogleSheetsApi;
import com.pervyilokon.models.Appointment;
import com.pervyilokon.models.ApiResponse;
import com.pervyilokon.models.RequestBody;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements AppointmentAdapter.OnAppointmentClickListener {
    
    private Toolbar toolbar;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefresh;
    private ProgressBar progressBar;
    private TextView tvStatus, tvSummary, tvSelectedDate;
    private MaterialButton btnPrevDay, btnToday, btnNextDay;
    private FloatingActionButton fabRefresh;
    
    private GoogleSheetsApi apiService;
    private AppointmentAdapter adapter;
    private List<Appointment> appointmentList = new ArrayList<>();
    private LocalDate selectedDate;
    private final DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("dd.MM.yyyy");
    private final DateTimeFormatter apiDateFormatter = DateTimeFormat.forPattern("yyyy-MM-dd");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        initViews();
        setupToolbar();
        setupRecyclerView();
        setupApi();
        setupClickListeners();
        
        selectedDate = new LocalDate();
        updateSelectedDateText();
        loadAppointments();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        recyclerView = findViewById(R.id.recyclerView);
        swipeRefresh = findViewById(R.id.swipeRefresh);
        progressBar = findViewById(R.id.progressBar);
        tvStatus = findViewById(R.id.tvStatus);
        tvSummary = findViewById(R.id.tvSummary);
        tvSelectedDate = findViewById(R.id.tvSelectedDate);
        btnPrevDay = findViewById(R.id.btnPrevDay);
        btnToday = findViewById(R.id.btnToday);
        btnNextDay = findViewById(R.id.btnNextDay);
        fabRefresh = findViewById(R.id.fabRefresh);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
    }

    private void setupRecyclerView() {
        adapter = new AppointmentAdapter(appointmentList, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void setupApi() {
        apiService = ApiClient.getApiService();
    }

    private void setupClickListeners() {
        swipeRefresh.setOnRefreshListener(this::loadAppointments);
        
        btnPrevDay.setOnClickListener(v -> {
            selectedDate = selectedDate.minusDays(1);
            updateSelectedDateText();
            loadAppointments();
        });
        
        btnToday.setOnClickListener(v -> {
            selectedDate = new LocalDate();
            updateSelectedDateText();
            loadAppointments();
        });
        
        btnNextDay.setOnClickListener(v -> {
            selectedDate = selectedDate.plusDays(1);
            updateSelectedDateText();
            loadAppointments();
        });
        
        fabRefresh.setOnClickListener(v -> loadAppointments());
    }

    private void updateSelectedDateText() {
        String dateText = selectedDate.toString(dateFormatter);
        if (selectedDate.isEqual(new LocalDate())) {
            dateText += " (Сегодня)";
        } else if (selectedDate.isEqual(new LocalDate().plusDays(1))) {
            dateText += " (Завтра)";
        } else if (selectedDate.isEqual(new LocalDate().minusDays(1))) {
            dateText += " (Вчера)";
        }
        tvSelectedDate.setText(dateText);
    }

    private void loadAppointments() {
        showLoading(true);
        String apiDate = selectedDate.toString(apiDateFormatter);
        
        apiService.getAppointments("getAppointments", apiDate).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                showLoading(false);
                swipeRefresh.setRefreshing(false);
                
                if (response.isSuccessful() && response.body() != null) {
                    List<Appointment> appointments = response.body().getAppointments();
                    if (appointments != null) {
                        appointmentList.clear();
                        appointmentList.addAll(appointments);
                        adapter.updateData(appointmentList);
                        
                        updateSummary(appointments);
                        tvStatus.setText("Загружено записей: " + appointments.size());
                    } else {
                        tvStatus.setText("Нет записей на выбранную дату");
                    }
                } else {
                    showError("Ошибка загрузки данных");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                showLoading(false);
                swipeRefresh.setRefreshing(false);
                showError("Ошибка сети: " + t.getMessage());
            }
        });
    }

    private void updateSummary(List<Appointment> appointments) {
        int newCount = 0;
        int confirmedCount = 0;
        int completedCount = 0;
        
        for (Appointment appointment : appointments) {
            switch (appointment.getStatus()) {
                case "новая":
                    newCount++;
                    break;
                case "подтверждена":
                    confirmedCount++;
                    break;
                case "выполнена":
                    completedCount++;
                    break;
            }
        }
        
        String summary = String.format("Всего: %d • Новые: %d • Подтвержденные: %d • Выполненные: %d", 
                appointments.size(), newCount, confirmedCount, completedCount);
        tvSummary.setText(summary);
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        if (show) {
            tvStatus.setText("Загрузка...");
        }
    }

    private void showError(String message) {
        tvStatus.setText("❌ " + message);
    }

    @Override
    public void onAppointmentClick(Appointment appointment) {
        showAppointmentDetails(appointment);
    }

    @Override
    public void onStatusClick(Appointment appointment) {
        showStatusDialog(appointment);
    }

    @Override
    public void onCompleteClick(Appointment appointment) {
        updateAppointmentStatus(appointment, "выполнена");
    }

    private void showAppointmentDetails(Appointment appointment) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Детали записи")
                .setMessage(createAppointmentDetails(appointment))
                .setPositiveButton("OK", null)
                .setNeutralButton("Изменить статус", (dialog, which) -> showStatusDialog(appointment))
                .show();
    }

    private String createAppointmentDetails(Appointment appointment) {
        return String.format(
                "👦 Имя: %s\n\n" +
                "📞 Телефон: %s\n\n" +
                "🎂 Возраст: %s лет\n\n" +
                "✂️ Услуга: %s\n\n" +
                "💰 Цена: %s руб\n\n" +
                "📅 Дата: %s %s\n\n" +
                "🏷️ Статус: %s\n\n" +
                "👩‍💼 Мастер: %s\n\n" +
                "📝 Примечания: %s",
                appointment.getClientName(),
                appointment.getPhone(),
                appointment.getChildAge(),
                appointment.getService(),
                appointment.getPrice(),
                appointment.getDate(),
                appointment.getTime(),
                appointment.getStatus(),
                appointment.getMaster() != null ? appointment.getMaster() : "Не назначен",
                appointment.getNotes() != null ? appointment.getNotes() : "Нет примечаний"
        );
    }

    private void showStatusDialog(Appointment appointment) {
        String[] statuses = {"новая", "подтверждена", "выполнена", "отменена"};
        
        new MaterialAlertDialogBuilder(this)
                .setTitle("Изменить статус")
                .setItems(statuses, (dialog, which) -> {
                    updateAppointmentStatus(appointment, statuses[which]);
                })
                .show();
    }

    private void updateAppointmentStatus(Appointment appointment, String newStatus) {
        showLoading(true);
        
        RequestBody request = new RequestBody("updateStatus", new RequestBody.StatusUpdate(
                appointment.getId(), newStatus
        ));
        
        apiService.updateAppointmentStatus(request).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    appointment.setStatus(newStatus);
                    adapter.notifyDataSetChanged();
                    loadAppointments();
                    
                    showMessage("Статус изменен на: " + newStatus);
                } else {
                    showError("Ошибка обновления статуса");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                showLoading(false);
                showError("Ошибка сети");
            }
        });
    }

    private void showMessage(String message) {
        tvStatus.setText("✅ " + message);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_settings) {
            showSettings();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showSettings() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Настройки")
                .setMessage("Здесь можно настроить URL API и другие параметры")
                .setPositiveButton("OK", null)
                .show();
    }
}
