package teksystems.casestudy.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import teksystems.casestudy.database.dao.AppointmentDAO;
import teksystems.casestudy.database.dao.ClinicianDAO;
import teksystems.casestudy.database.dao.PatientDAO;
import teksystems.casestudy.database.dao.UserDAO;
import teksystems.casestudy.database.entity.*;
import teksystems.casestudy.formbean.RegisterFormBean;
import teksystems.casestudy.formbean.SelectAppointmentScheduleFormBean;

import javax.validation.Valid;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

@Slf4j
@Controller
public class AppointmentController {

    private final String[] appointmentTimes = {"08:00", "08:30", "09:00", "09:30", "10:00",
            "10:30", "11:00", "11:30", "12:00", "12:00", "01:00", "01:30",
            "02:00", "02:30", "03:00", "03:30", "04:00"};

    @Autowired
    private AppointmentDAO appointmentDao;

    @Autowired
    private ClinicianDAO clinicianDao;

    @Autowired
    private PatientDAO patientDao;

    @Autowired
    private UserDAO userDao;

//    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//    String currentPrincipalName = authentication.getName();
//
//    User user = userDao.findByEmail(currentPrincipalName);

    //TO DO: Add drop-down with DATE and clinicianId
    //TO DO: Update table after clicking submit button
    //store all clinicianId in a list, pass to front-end to use in drop down
    //store all dates in a drop-down (as strings), maybe have four drop downs?
    //handle null entry, when page is loaded initially

    @RequestMapping(value= "/user/schedule_appointment", method = RequestMethod.GET)
    public ModelAndView schedule(@RequestParam(required = false) Integer userId,
                                 @RequestParam(required = false) Integer year,
                                 @RequestParam(required = false) Integer month,
                                 @RequestParam(required = false) Integer day) throws Exception {

        ModelAndView response = new ModelAndView();
        response.setViewName("user/schedule_appointment");

        Clinician clinician = (userId != null) ? clinicianDao.findByUserId(userId)
                : clinicianDao.findByClinicianId(4);

        Integer clinicianId = (userId != null) ? clinician.getClinicianId() : 4;

        if(day == null) {
            day = 4;
        }
        if(month == null) {
            month = 4;
        }
        if(year == null) {
            year = 2022;
        }

        User user = userDao.findByUserId(clinician.getUserId());

        log.info(user.toString());

        LocalDate date = LocalDate.of(year, month, day);

        List<Appointment> appointments = appointmentDao.findByClinicianClinicianIdAndDate(clinicianId, date);

        Set<String> scheduledTime = new HashSet<>();

        for (Appointment appointment : appointments) {
            scheduledTime.add(appointment.getTime().toString());
        }

        List<Clinician> clins = clinicianDao.findAll();
        List<User> clinicianUsers = new ArrayList<>();

        for (Clinician clin : clins) {
            User clinUser = userDao.findByUserId(clin.getUserId());
            clinicianUsers.add(clinUser);
        }


//        response.setViewName("user/schedule_appointment"); //getting the jsp file
        response.addObject("clinicianUsers", clinicianUsers);
        response.addObject("clinUser", user);
        response.addObject("scheduledTime", scheduledTime);
        response.addObject("localDate", date);
        response.addObject("clinicianId", clinicianId);
//        response.addObject("user", user);
        response.addObject("appointmentTimes", appointmentTimes);

//        SelectAppointmentScheduleFormBean form = new SelectAppointmentScheduleFormBean();
//        response.addObject("form", form);

        //attributeName is object inside of jsp, and scheduledTime is the object that is being passed to that name

        return response;
    }

    @PostMapping(value= "/user/schedule_appointmentSubmit")
    public ModelAndView appointmentSubmit(@Valid SelectAppointmentScheduleFormBean form) throws Exception {
        ModelAndView response = new ModelAndView();

        Appointment appointment = new Appointment();
        Clinician clinician = clinicianDao.findByUserId(form.getUserId());
        appointment.setClinician(clinician);
        appointment.setDate(LocalDate.parse(form.getDate()));
        appointment.setTime(LocalTime.parse(form.getTime()));
        appointmentDao.save(appointment);


//      TODO: response.setViewName("redirect:/user/schedule_appointment" + userId);
//        response.setViewName("redirect:/user/my_schedule/" + user.getUserId());

        return response;

    }

    @RequestMapping(value= "/user/my_schedule/{userId}", method = RequestMethod.GET)
    public ModelAndView myAppointments(@PathVariable("userId") Integer userId) {
        ModelAndView response = new ModelAndView();
        response.setViewName("user/my_schedule");



        List<Appointment> appointments = appointmentDao.findByPatientPatientId(userId);

        log.info(appointments.toString());
        log.info(userId.toString());

//        String name = " ";

//        if(userId != null) {
        User user = userDao.findByUserId(userId);

//            name = patient.getFirstName();
//        }

        response.addObject("appointments", appointments);
        response.addObject("user", user);

//        response.setViewName("redirect:/user/my_schedule/paq");
        return response;
    }

    @RequestMapping(value= "/user/my_schedule/", method = RequestMethod.GET)
    public ModelAndView navigateToPaq(@RequestParam(required = false) Integer userId) {
        ModelAndView response = new ModelAndView();
        response.setViewName("user/paq");

        response.setViewName("redirect:/user/paq");
        return response;
    }
}
