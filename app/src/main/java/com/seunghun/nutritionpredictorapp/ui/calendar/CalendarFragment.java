package com.seunghun.nutritionpredictorapp.ui.calendar;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.kizitonwose.calendar.core.CalendarDay;
import com.kizitonwose.calendar.core.DayPosition;
import com.kizitonwose.calendar.view.CalendarView;
import com.kizitonwose.calendar.view.MonthDayBinder;
import com.kizitonwose.calendar.view.ViewContainer;
import com.seunghun.nutritionpredictorapp.R;
import com.seunghun.nutritionpredictorapp.data.db.IntakeRepository;
import com.seunghun.nutritionpredictorapp.databinding.FragmentCalendarBinding;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import kotlin.Unit;

public class CalendarFragment extends Fragment {

    private FragmentCalendarBinding binding;
    private IntakeRepository repo;

    private final DateTimeFormatter ymFmt = DateTimeFormatter.ofPattern("yyyy-MM", Locale.getDefault());
    private final DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.getDefault());

    private Map<String, List<String>> monthLabels; // "yyyy-MM-dd" -> 음식명 리스트
    private YearMonth currentMonth;

    private void setMonthTitle(YearMonth ym) {
        binding.tvMonth.setText(ym.format(ymFmt));
    }

    // Day cell holder
    public static class DayViewContainer extends ViewContainer {
        TextView tvDay;
        TextView tvFoods;
        CalendarDay day; // 현재 바인딩된 날짜

        public DayViewContainer(@NonNull View view) {
            super(view);
            tvDay = view.findViewById(R.id.tvDay);
            tvFoods = view.findViewById(R.id.tvFoods);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentCalendarBinding.inflate(inflater, container, false);
        repo = new IntakeRepository(requireContext());

        CalendarView calendarView = binding.calendarView;

        // 1) 달력 범위 설정
        currentMonth = YearMonth.now();
        setMonthTitle(currentMonth);
        YearMonth startMonth = currentMonth.minusMonths(24);
        YearMonth endMonth = currentMonth.plusMonths(24);

        DayOfWeek firstDayOfWeek = DayOfWeek.SUNDAY; // 한국은 보통 일요일 시작(원하면 MONDAY로 변경)
        calendarView.setup(startMonth, endMonth, firstDayOfWeek);
        calendarView.scrollToMonth(currentMonth);

        // 2) 현재 월 데이터 로드
        loadMonth(currentMonth);

        // 3) DayBinder 연결 (날짜/음식명 표시 + 클릭 이동)
        calendarView.setDayBinder(new MonthDayBinder<DayViewContainer>() {
            @NonNull
            @Override
            public DayViewContainer create(@NonNull View view) {
                return new DayViewContainer(view);
            }

            @Override
            public void bind(@NonNull DayViewContainer container, @NonNull CalendarDay data) {
                container.day = data;

                LocalDate date = data.getDate();
                String dateStr = date.format(dateFmt);

                // 달력의 이번 달 날짜만 진하게/표시하고 싶으면 DayPosition.MonthDate만 사용
                if (data.getPosition() == DayPosition.MonthDate) {
                    container.tvDay.setText(String.valueOf(date.getDayOfMonth()));

                    List<String> labels = monthLabels != null ? monthLabels.get(dateStr) : null;
                    container.tvFoods.setText(summarizeLabels(labels));

                    // 클릭 시 상세 화면 이동(원하는 방식으로 교체)
                    container.getView().setOnClickListener(v -> {
                        if (data.getPosition() != DayPosition.MonthDate) return;
                        Bundle b = new Bundle();
                        b.putString("arg_date", dateStr);
                        Navigation.findNavController(v).navigate(R.id.action_calendar_to_dayDetail, b);
                    });

                } else {
                    // in/out date
                    container.tvDay.setText("");
                    container.tvFoods.setText("");
                    container.getView().setOnClickListener(null);
                }
            }
        });

        // 4) 월 스크롤 시 그 달 데이터 다시 로드(선택)
        calendarView.setMonthScrollListener(month -> {
            currentMonth = month.getYearMonth();
            setMonthTitle(currentMonth);
            loadMonth(currentMonth);
            return Unit.INSTANCE;
        });

        binding.btPrev.setOnClickListener(v -> {
            YearMonth target = currentMonth.minusMonths(1);
            calendarView.smoothScrollToMonth(target); // 핵심: 실제 월 이동
        });

        binding.btNext.setOnClickListener(v -> {
            YearMonth target = currentMonth.plusMonths(1);
            calendarView.smoothScrollToMonth(target); // 핵심: 실제 월 이동
        });

        return binding.getRoot();
    }

    /** 해당 월의 일자별 음식명 리스트 얻음. */
    private void loadMonth(YearMonth ym) {
        String ymStr = ym.format(ymFmt);
        monthLabels = repo.getMonthFoodLabels(ymStr); // 해당 월의 일자별 음식명 리스트 얻음.

        // monthLabels만 바뀌면 기존 셀을 다시 그려야 음식명 표시가 갱신됩니다.
        // invalidate() 역할: 보이는 영역 다시 바인딩
        binding.calendarView.notifyCalendarChanged();
    }

    /** 음식명들을 한 String으로 합침. */
    private String summarizeLabels(List<String> labels) {
        if (labels == null || labels.isEmpty()) return "";
        if (labels.size() == 1) return labels.get(0);
        if (labels.size() == 2) return labels.get(0) + ", " + labels.get(1);
        return labels.get(0) + ", " + labels.get(1) + " +" + (labels.size() - 2);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

