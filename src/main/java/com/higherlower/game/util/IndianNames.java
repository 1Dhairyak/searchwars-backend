package com.higherlower.game.util;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Pool of Indian full names given to players who skip choosing one.
 * These are not reserved — two players can get the same random name.
 */
public final class IndianNames {

    private IndianNames() {}

    public static final List<String> NAMES = List.of(
        // Hindu
        "Aarav Sharma", "Vivaan Gupta", "Aditya Verma", "Arjun Reddy", "Sai Krishnan",
        "Ishaan Mehta", "Rohan Iyer", "Karthik Nair", "Ananya Joshi", "Diya Patel",
        "Priya Menon", "Kavya Rao", "Sneha Kulkarni", "Pooja Desai", "Neha Agarwal",
        "Rahul Chatterjee", "Siddharth Banerjee", "Vikram Malhotra", "Aditi Bhatt", "Meera Pillai",
        "Lakshmi Subramanian", "Arnav Saxena", "Riya Kapoor", "Tanvi Deshpande", "Nikhil Joshi",
        "Harsh Vaidya", "Shreya Kamath", "Varun Chopra", "Ishita Bose", "Manish Tiwari",
        "Deepika Hegde", "Pranav Shetty", "Anjali Mishra", "Gaurav Pandey", "Swati Chauhan",
        "Akash Yadav", "Nandini Krishnan", "Ravi Bhosale", "Divya Bhattacharya", "Suresh Bhat",
        "Kiran Patil", "Aishwarya Rajan", "Vishal Thakur", "Pallavi Jain", "Abhishek Sinha",
        "Krithika Venkatesh", "Mohit Arora", "Sanjana Naidu", "Yash Choudhary", "Rekha Dubey",
        "Tejas Gokhale", "Anushka Sen", "Rajesh Kumar", "Bhavna Trivedi", "Omkar Jadhav",
        "Shruti Kale", "Dev Rastogi", "Gayatri Srinivasan", "Kunal Bajaj", "Madhuri Apte",
        // Muslim
        "Ayesha Khan", "Zoya Siddiqui", "Imran Qureshi", "Faisal Ahmed", "Sana Shaikh",
        "Arif Ansari", "Nazia Hussain", "Rehan Malik", "Farhan Rahman", "Aamir Mirza",
        "Zainab Rizvi", "Salman Pathan", "Hina Syed", "Irfan Baig", "Mehreen Kazmi",
        "Adil Rashid", "Shabana Parveen", "Tariq Hashmi", "Nida Farooqui", "Yusuf Lakdawala",
        "Rukhsar Begum", "Danish Iqbal", "Amaan Chaudhry", "Sara Merchant", "Kabir Sheikh",
        "Alina Nawab", "Junaid Bukhari", "Fatima Zaidi", "Owais Kidwai", "Huma Usmani",
        // Sikh
        "Simran Kaur", "Harpreet Singh", "Gurpreet Kaur", "Jaspreet Singh", "Manpreet Kaur",
        "Amandeep Singh", "Navjot Randhawa", "Ravneet Kaur", "Harjeet Gill", "Kuldeep Sandhu",
        "Parminder Dhillon", "Baljit Grewal", "Jasleen Kaur", "Gurleen Bedi", "Tejinder Bains",
        "Arshdeep Toor", "Inderjit Chahal", "Sukhmani Kaur", "Rajveer Brar", "Kirandeep Virk",
        "Mandeep Sekhon", "Harleen Kaur", "Jagmeet Saini", "Noor Kaur", "Gurvinder Mann"
    );

    /**
     * Removed from the pool (Christian, Parsi, Northeast). Leaderboard rows still
     * carrying one of these as a random name are renamed at startup — see AnonymousNameBackfill.
     */
    public static final Set<String> RETIRED = Set.of(
        "Joseph D'Souza", "Maria Fernandes", "Anthony Pereira", "Priya D'Costa", "Ryan Rodrigues",
        "Sharon Thomas", "Jacob Mathew", "Teresa Gomes", "Kevin Pinto", "Neha Lobo",
        "Vincent Dias", "Angela Coutinho", "George Kurian", "Elizabeth Varghese", "Samuel Joseph",
        "Rebecca Abraham", "Francis Noronha", "Clara Saldanha", "Thomas Chacko", "Divya George",
        "Allen Cherian", "Grace Philip", "Melvin Monteiro", "Shirley Mascarenhas", "Daniel Kuruvilla",
        "Cyrus Daruwala", "Zubin Irani", "Tanaz Wadia", "Farokh Contractor", "Dinaz Bharucha",
        "Tenzin Dolma", "Lalremruati Pachuau", "Karma Bhutia", "Mary Sangma", "Pema Wangchuk"
    );

    public static String random() {
        return NAMES.get(ThreadLocalRandom.current().nextInt(NAMES.size()));
    }
}
