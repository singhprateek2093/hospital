package com.hospital.config;

import com.hospital.model.*;
import com.hospital.repo.AppointmentRepository;
import com.hospital.repo.DoctorRepository;
import com.hospital.repo.PatientRepository;
import com.hospital.repo.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * Seeds demo data on startup IF the database is empty, so you can log in and click
 * around immediately. All credentials below are fake — learning data only.
 *
 * Multispeciality Hospital — Hyderabad, Telangana, India.
 *
 *   rakesh@hospital.in    / admin123       (ADMIN  - Rakesh Ramgiri)
 *   kamlesh@hospital.in   / doctor123      (DOCTOR - Dr. Kamlesh Ramgiri, Cardiology)
 *   ravali@hospital.in    / doctor123      (DOCTOR - Dr. Ravali Ramgiri, Neurology)
 *   reception@hospital.in / reception123   (RECEPTIONIST - Sunita Reddy)
 */
@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner seed(UserRepository users,
                           DoctorRepository doctors,
                           PatientRepository patients,
                           AppointmentRepository appointments,
                           PasswordEncoder encoder) {
        return args -> {
            if (users.count() > 0) {
                return; // already seeded
            }

            // --- accounts ---
            User admin = users.save(new User(
                    "Rakesh Ramgiri", "rakesh@hospital.in", encoder.encode("admin123"), Role.ADMIN));

            User reception = users.save(new User(
                    "Sunita Reddy", "reception@hospital.in", encoder.encode("reception123"), Role.RECEPTIONIST));

            User kamleshUser = users.save(new User(
                    "Dr. Kamlesh Ramgiri", "kamlesh@hospital.in", encoder.encode("doctor123"), Role.DOCTOR));
            Doctor kamlesh = doctors.save(new Doctor(kamleshUser, "Cardiology"));

            User ravaliUser = users.save(new User(
                    "Dr. Ravali Ramgiri", "ravali@hospital.in", encoder.encode("doctor123"), Role.DOCTOR));
            Doctor ravali = doctors.save(new Doctor(ravaliUser, "Neurology"));

            // --- patients (some with intake history) ---
            Patient aarav = new Patient("Aarav Sharma", 42, "M", "+91 98480 01010", kamlesh, admin);
            aarav.addIntakeForm(new IntakeForm("fever, cough", "BP 120/80, T 101F", "symptoms 3 days"));
            patients.save(aarav);

            Patient diya = new Patient("Diya Patel", 35, "F", "+91 98480 01044", kamlesh, reception);
            diya.addIntakeForm(new IntakeForm("chest tightness", "BP 130/85", "follow-up in 1 week"));
            patients.save(diya);

            Patient vivaan = new Patient("Vivaan Reddy", 28, "M", "+91 98480 01077", ravali, admin);
            vivaan.addIntakeForm(new IntakeForm("migraines", "BP 118/76", "prescribed rest"));
            patients.save(vivaan);

            // one unassigned patient, to show the admin-only view
            patients.save(new Patient("Ananya Iyer", 50, "F", "+91 98480 01099", null, reception));

            // A batch of extra patients so pagination (10/page) is visible in the UI.
            String[] names = {
                "Arjun Nair", "Saanvi Rao", "Reyansh Gupta", "Aadhya Menon", "Kabir Singh",
                "Ishita Verma", "Vihaan Kumar", "Myra Joshi", "Aryan Choudhary", "Anika Desai"
            };
            String[] genders = {"M", "F", "M", "F", "M", "F", "M", "F", "M", "F"};
            for (int i = 0; i < names.length; i++) {
                Doctor doc = (i % 2 == 0) ? kamlesh : ravali; // alternate assignment
                patients.save(new Patient(
                        names[i], 20 + i, genders[i], "+91 98480 02" + (10 + i), doc, admin));
            }

            // --- some appointments (soonest first in the UI) ---
            Instant now = Instant.now();
            appointments.save(new Appointment(aarav, kamlesh, now.plus(1, ChronoUnit.DAYS),
                    "Follow-up: chest pain", admin));
            appointments.save(new Appointment(diya, kamlesh, now.plus(2, ChronoUnit.DAYS),
                    "Routine cardiology check", reception));
            appointments.save(new Appointment(vivaan, ravali, now.plus(3, ChronoUnit.HOURS),
                    "Migraine consultation", admin));
            Appointment past = new Appointment(aarav, kamlesh, now.minus(2, ChronoUnit.DAYS),
                    "Initial visit", admin);
            past.setStatus(AppointmentStatus.COMPLETED);
            appointments.save(past);
        };
    }
}
