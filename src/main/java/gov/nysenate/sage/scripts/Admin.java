package gov.nysenate.sage.scripts;

import gov.nysenate.sage.dao.model.ApiUserDao;
import gov.nysenate.sage.dao.model.JobUserDao;
import gov.nysenate.sage.factory.ApplicationFactory;
import gov.nysenate.sage.model.api.ApiUser;
import gov.nysenate.sage.model.job.JobUser;
import gov.nysenate.sage.util.Config;
import gov.nysenate.sage.util.auth.ApiUserAuth;
import gov.nysenate.sage.util.auth.JobUserAuth;

import java.util.List;
import java.util.Scanner;

public class Admin
{
    public Config config;

    public Admin() {}

    public static void main(String[] args)
    {
        if (args.length == 1) {
            System.err.println("This script is used to manage API and Batch Job Users.");
            System.err.println("Usage: Admin --viewApiUsers --createApiUser --deleteApiUser");
            System.err.println("             --viewJobUsers --createJobUser --deleteJobUser");
            System.exit(-1);
        }

        /** Load up the configuration settings */
        if (!ApplicationFactory.bootstrap()){
            System.err.println("Failed to configure application config");
            System.exit(-1);
        }

        Admin admin = new Admin();
        String arg = args[1];

        if (arg.equalsIgnoreCase("--viewApiUsers")) {
            admin.viewApiUsers();
        }
        else if (arg.equalsIgnoreCase("--createApiUser")) {
            admin.createApiUser();
        }
        else if (arg.equalsIgnoreCase("--deleteApiUser")) {
            admin.deleteApiUser();
        }
        else if (arg.equalsIgnoreCase("--viewJobUsers")) {
            admin.viewJobUsers();
        }
        else if (arg.equalsIgnoreCase("--createJobUser")) {
            admin.createJobUser();
        }
        else if (arg.equalsIgnoreCase("--deleteJobUser")) {
            admin.deleteJobUser();
        }
        else {
            System.err.println("Invalid command!");
        }

        System.exit(0);
    }

    private void viewApiUsers()
    {
        ApiUserDao apiUserDao = new ApiUserDao();
        List<ApiUser> apiUserList = apiUserDao.getApiUsers();

        if (apiUserList != null) {
            System.out.println("List of Api Users");
            System.out.println("Id | Key | Name | Desc");
            for (ApiUser apiUser : apiUserList) {
                System.out.println(apiUser.getId() + " | " + apiUser.getApiKey() + " | " + apiUser.getName() + " | " + apiUser.getDescription());
            }
        }
        else {
            System.err.println("Failed to retrieve user list");
        }
    }

    private void createApiUser()
    {
        Scanner scanner = new Scanner(System.in);
        String name, desc;

        System.out.println("Create new API User");
        System.out.println("--------------------");
        System.out.print("Name of new user: "); name = scanner.nextLine();
        System.out.print("Description of new user: "); desc = scanner.nextLine();

        ApiUserAuth apiUserAuth = new ApiUserAuth();
        ApiUser apiUser = apiUserAuth.addApiUser(name, desc);

        if (apiUser != null) {
            System.out.println("Sucessfully created user. The API key for the user is displayed below.");
            System.out.println(apiUser.getApiKey());
        }
        else {
            System.err.println("Failed to create api user!");
        }
    }

    private void deleteApiUser()
    {
        Scanner scanner = new Scanner(System.in);
        String name;

        System.out.println("Delete existing API User");
        System.out.println("--------------------");
        System.out.print("Name of user to delete: "); name = scanner.nextLine();

        ApiUserDao apiUserDao = new ApiUserDao();
        ApiUser apiUser = apiUserDao.getApiUserByName(name);

        if (apiUser != null) {
            if (apiUserDao.removeApiUser(apiUser) == 1 ) {
                System.out.println("Deleted user: " + name);
            }
        }
        else {
            System.err.println("Failed to find api user with name: " + name);
        }
    }

    private void viewJobUsers()
    {
        JobUserDao jobUserDao = new JobUserDao();
        List<JobUser> jobUsers = jobUserDao.getJobUsers();

        if (jobUsers != null) {
            System.out.println("List of Job Users");
            System.out.println("Id | Email | Name");
            for (JobUser jobUser : jobUsers) {
                System.out.println(jobUser.getId() + " | " + jobUser.getEmail() + " | " + jobUser.getFirstname() + " " + jobUser.getLastname());
            }
        }
        else {
            System.err.println("Failed to retrieve job user list");
        }
    }

    private void createJobUser()
    {
        Scanner scanner = new Scanner(System.in);
        String firstname, lastname, email, password;

        System.out.println("Create new Job User");
        System.out.println("--------------------");

        System.out.print("First Name: "); firstname = scanner.nextLine();
        System.out.print("Last Name: "); lastname = scanner.nextLine();
        System.out.print("Email: "); email = scanner.nextLine();
        System.out.print("Password: "); password = scanner.nextLine();

        JobUserAuth jobUserAuth = new JobUserAuth();
        JobUser jobUser = jobUserAuth.addActiveJobUser(email, password, firstname, lastname);

        if (jobUser != null) {
            System.out.println("Successfully created job user with id " + jobUser.getId());
        }
        else {
            System.out.println("Failed to create job user");
        }
    }

    private void deleteJobUser()
    {
        Scanner scanner = new Scanner(System.in);
        String email;

        System.out.println("Delete existing API User");
        System.out.println("-------------------------");
        System.out.print("Email of user to remove: "); email = scanner.nextLine();

        JobUserDao jobUserDao = new JobUserDao();
        JobUser jobUser = jobUserDao.getJobUserByEmail(email);

        if (jobUser != null) {
            if (jobUserDao.removeJobUser(jobUser) == 1) {
                System.out.println("Successfully removed job user " + email);
            }
        }
        else {
            System.out.println("Failed to find job user with email " + email);
        }
    }
}
