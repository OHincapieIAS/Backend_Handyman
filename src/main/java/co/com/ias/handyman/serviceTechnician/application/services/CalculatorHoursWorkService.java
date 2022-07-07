package co.com.ias.handyman.serviceTechnician.application.services;

import co.com.ias.handyman.infranstructure.models.CalculatorHoursDTO;
import co.com.ias.handyman.infranstructure.models.ServiceTechnicianDTO;
import co.com.ias.handyman.serviceTechnician.application.domain.ServiceTechnician;
import co.com.ias.handyman.serviceTechnician.application.ports.input.CalculatorHoursWorkUseCase;
import co.com.ias.handyman.serviceTechnician.application.ports.output.ServiceTechnicianRepository;
import co.com.ias.handyman.technician.application.domain.valueObjs.TechnicianId;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class CalculatorHoursWorkService implements CalculatorHoursWorkUseCase {

    private final ServiceTechnicianRepository serviceTechnicianRepository;

    public CalculatorHoursWorkService(ServiceTechnicianRepository serviceTechnicianRepository) {
        this.serviceTechnicianRepository = serviceTechnicianRepository;
    }

    @Override
    public Optional<List<CalculatorHoursDTO>> execute(ServiceTechnicianDTO serviceTechnicianDTO) {
        Double hoursAtWeek = 0.0;
        Optional<List<ServiceTechnician>> serviceTechnician = serviceTechnicianRepository.getServiceTechnicianBetweenDates(new TechnicianId(serviceTechnicianDTO.getIdTechnician()), serviceTechnicianDTO.getStartDate(), serviceTechnicianDTO.getFinalDate());
        System.out.println(serviceTechnician.get());
        List<CalculatorHoursDTO> calculatorSomeHoursDTO = new ArrayList<>();
        if (serviceTechnician.isPresent()) {

            for(ServiceTechnician st : serviceTechnician.get()) {
                CalculatorHoursDTO calculatorHoursDTO = CalculatorHoursDTO.fromDomain(st);
                Map<String, Double> hoursMap = calculateHours(calculatorHoursDTO, serviceTechnicianDTO.getStartDate(), serviceTechnicianDTO.getFinalDate(), hoursAtWeek);
                calculatorHoursDTO.setRegularHours(hoursMap.get("RegularHour"));
                calculatorHoursDTO.setNightHour(hoursMap.get("NightHour"));
                calculatorHoursDTO.setRegularHourExtra(hoursMap.get("RegularHourExtra"));
                calculatorHoursDTO.setNightHourExtra(hoursMap.get("NightHourExtra"));
                calculatorHoursDTO.setSundayHour(hoursMap.get("SundayHour"));
                calculatorHoursDTO.setSundayHourExtra(hoursMap.get("SundayHourExtra"));
                calculatorHoursDTO.setTotalHoursByService(hoursMap.get("TotalHoursByService"));
                calculatorHoursDTO.setTotalHours(hoursMap.get("TotalHours"));
                hoursAtWeek = hoursMap.get("TotalHours");
                calculatorSomeHoursDTO.add(calculatorHoursDTO);
            }
            return Optional.of(calculatorSomeHoursDTO);
        } else {
            return Optional.empty();
        }
    }

    public Map<String, Double> calculateHours(CalculatorHoursDTO calculator, LocalDateTime startDateFromUser, LocalDateTime finalDateFromUser, Double hoursWorkedAtWeek) {
        Double hoursWorkedByService, hNoc, hNor, hNocEx, hNorEx, hDom, hDomEx ;
        hoursWorkedByService= hNoc= hNor= hNocEx= hNorEx= hDom= hDomEx = 0.0;
        LocalDateTime startDateAux, finalDateAux;
        startDateAux =  LocalDateTime.now();
        finalDateAux = LocalDateTime.now();
        if(calculator.getStartDate().isAfter(startDateFromUser) && calculator.getFinalDate().isBefore(finalDateFromUser)) {
            startDateAux = calculator.getStartDate();
            finalDateAux = calculator.getFinalDate();
        } else if(calculator.getStartDate().isAfter(startDateFromUser) && calculator.getFinalDate().isAfter(finalDateFromUser)) {
            startDateAux = calculator.getStartDate();
            finalDateAux = finalDateFromUser;
        } else if(calculator.getStartDate().isBefore(startDateFromUser) && calculator.getFinalDate().isBefore(finalDateFromUser)) {
            startDateAux = startDateFromUser;
            finalDateAux = calculator.getFinalDate();
        }


        Map<String, Double> hours = new HashMap<String, Double>();
        while (startDateAux.isBefore(finalDateAux)) {
            System.out.println(startDateAux);
            for (int i =0; startDateAux.getHour() <= 23 && startDateAux.isBefore(finalDateAux); i++){
                //Validar que las horas  y fechas sean iguales y a partir de ahi trabajar sobre minutos
                String qualifyHour = qualifyHours(startDateAux, hoursWorkedAtWeek);
                switch (qualifyHour) {
                    case "HNOC" -> {
                        hNoc++;
                        hoursWorkedAtWeek++;
                        hoursWorkedByService++;
                    }
                    case "HNOR" -> {
                        hNor++;
                        hoursWorkedAtWeek++;
                        hoursWorkedByService++;
                    }
                    case "HNOCEXTRA" -> {
                        hNocEx++;
                        hoursWorkedAtWeek++;
                        hoursWorkedByService++;
                    }
                    case "HNOREXTRA" -> {
                        hNorEx++;
                        hoursWorkedAtWeek++;
                        hoursWorkedByService++;
                    }
                    case "HDOM" -> {
                        hDom++;
                        hoursWorkedAtWeek++;
                        hoursWorkedByService++;
                    }
                    case "HDOMEXTRA" -> {
                        hDomEx++;
                        hoursWorkedAtWeek++;
                        hoursWorkedByService++;
                    }
                    default -> {
                    }
                }
                startDateAux = startDateAux.plusHours(1);
            }
        }
        hours.put("RegularHour", hNor);
        hours.put("NightHour", hNoc);
        hours.put("RegularHourExtra", hNorEx);
        hours.put("NightHourExtra", hNocEx);
        hours.put("SundayHour", hDom);
        hours.put("SundayHourExtra", hDomEx);
        hours.put("TotalHoursByService", hoursWorkedByService);
        hours.put("TotalHours", hoursWorkedAtWeek);
        return hours;
    }


    public String qualifyHours(LocalDateTime dateToAnalyze, Double hoursWorkedAtWeek) {
        if(dateToAnalyze.getDayOfWeek() != DayOfWeek.SUNDAY) {
            if(((dateToAnalyze.getHour() >= 20 && dateToAnalyze.getMinute() > 0 )|| dateToAnalyze.getHour() < 7) && hoursWorkedAtWeek < 48) {
                System.out.println(dateToAnalyze);
                return "HNOC";
            } else if((dateToAnalyze.getHour() >= 7 || dateToAnalyze.getHour() <= 20) && hoursWorkedAtWeek < 48) {
                return "HNOR";
            } else if(((dateToAnalyze.getHour() >= 20 && dateToAnalyze.getMinute() > 0 )|| dateToAnalyze.getHour() < 7) && hoursWorkedAtWeek >= 48) {
                return "HNOCEXTRA";
            }
            else if((dateToAnalyze.getHour() >= 7 || dateToAnalyze.getHour() <= 20) && hoursWorkedAtWeek >= 48) {
                return "HNOREXTRA";
            }
        } else {
            if(hoursWorkedAtWeek >= 48) {
                return "HDOMEXTRA";
            } else {
                return "HDOM";
            }
        }
        return "";
    }
}
