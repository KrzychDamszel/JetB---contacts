package contacts;

import java.io.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {
    public static void main(String[] args) {

        File file = new File("");
        if (args.length != 0) {
            file = new File(args[0]);
            System.out.println("open " + file.getName());
            System.out.println();
        }
        List<Record> records = new ArrayList<>();
        final Scanner scanner = new Scanner(System.in);

        if (file.isFile()) {
            try {
                records = (List<Record>) SerializationUtils.deserialize(file.getName());
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        InfoBook infoBook = new InfoBook(records);

        while (true) {
            System.out.print("[menu] Enter action (add, list, search, count, exit): ");
            String action = scanner.nextLine().trim().toLowerCase();

            switch (action) {
                case "add":
                    infoBook.add();
                    SerializationUtils.saveBase(args.length, records, file.getName());
                    break;
                case "list":
                    infoBook.list();
                    SerializationUtils.saveBase(args.length, records, file.getName());
                    break;
                case "search":
                    infoBook.search();
                    SerializationUtils.saveBase(args.length, records, file.getName());
                    break;
                case "count":
                    infoBook.count();
                    break;
                case "exit":
                    SerializationUtils.saveBase(args.length, records, file.getName());
                    return;
            }
        }

    }
}

class InfoBook {
    private List<Record> records;
    InfoBook (List<Record> records) {
        this.records = records;
    }
    Scanner scanner = new Scanner(System.in);

    public void add() {
        boolean flag = true;
        while (flag) {
            System.out.print("Enter the type (person, organization): ");
            String type = scanner.nextLine().trim().toLowerCase();
            String phoneNumber;

            switch (type) {
                case "person":
                    System.out.print("Enter the name: ");
                    String name = scanner.nextLine().trim();
                    System.out.print("Enter the surname: ");
                    String surname = scanner.nextLine().trim();
                    LocalDate birthDate = null;
                    System.out.print("Enter the birth date: ");
                    try {
                        birthDate = LocalDate.parse(scanner.nextLine().trim());
                    } catch (DateTimeParseException exc) {
                        System.out.println("Bad birth date!");
                    }
                    System.out.print("Enter the gender (M, F): ");
                    String gender = scanner.nextLine().trim().toUpperCase();
                    if (!"M".equals(gender) && !"F".equals(gender)) {
                        System.out.println("Bad gender!");
                        gender = "[no data]";
                    }
                    System.out.print("Enter the number: ");
                    phoneNumber = scanner.nextLine().trim();
                    if (!ValidNumber.isValidNumber(phoneNumber)) {
                        System.out.println("Wrong number format!");
                        phoneNumber = "[no number]";
                    }
                    records.add(new Person(name, surname, birthDate, gender, phoneNumber, LocalDateTime.now(), LocalDateTime.now()));
                    System.out.println("The record added.\n");
                    flag = false;
                    break;

                case "organization":
                    System.out.print("Enter the organization name: ");
                    String organizationName = scanner.nextLine().trim();
                    System.out.print("Enter the address: ");
                    String address = scanner.nextLine().trim();
                    System.out.print("Enter the number: ");
                    phoneNumber = scanner.nextLine().trim();
                    if (!ValidNumber.isValidNumber(phoneNumber)) {
                        System.out.println("Wrong number format!");
                        phoneNumber = "[no number]";
                    }
                    records.add(new Organization(organizationName, address, phoneNumber, LocalDateTime.now(), LocalDateTime.now()));
                    System.out.println("The record added.\n");
                    flag = false;
                    break;
            }
        }
    }

    public void list() {
        if (records.size() == 0) {
            System.out.println("No records!");
            return;
        }
        listRecords();
        String action;
        Record record;
        while (true) {
            System.out.print("[list] Enter action ([number], back): ");
            action = scanner.nextLine().trim().toUpperCase();
            if ("BACK".equals(action)) {
                System.out.println();
                return;
            }
            try {
                int index = Integer.parseInt(action);
                record = records.get(index - 1);
                System.out.println(record.toString());
                break;
            } catch (Exception e) {
                System.out.println("Wrong action/number!");
            }
        }
        while (true) {
            System.out.print("[record] Enter action (edit, delete, menu): ");
            action = scanner.nextLine().trim().toUpperCase();
            switch (action) {
                case "MENU":
                    System.out.println();
                    return;
                case "EDIT":
                    record.changeFields();
                    System.out.println(record.toString());
                    break;
                case "DELETE":
                    listRecords();
                    while (true) {
                        try {
                            System.out.print("Select a record: ");
                            int index = Integer.parseInt(scanner.nextLine());
                            records.remove(index - 1);
                            break;
                        } catch (Exception e) {
                            System.out.println("Wrong number");
                        }
                    }
                    System.out.println("The record deleted!\n");
                    System.out.println();
                    return;
            }
        }
    }

    public void search() {
        if (records.size() == 0) {
            System.out.println("No records!");
            return;
        }
        while (true) {
            System.out.print("Enter search query: ");
            String findString = scanner.nextLine();
            ArrayList<Integer> indexesFound = searching(findString);
            System.out.println("Found " + indexesFound.size() + " results:");
            for (int i = 0; i < indexesFound.size(); i++) {
                System.out.println((i + 1) + ". " + records.get(indexesFound.get(i)).nameList());
            }
            System.out.println();
            while (true) {
                System.out.print("[search] Enter action ([number], back, again): ");
                String action = scanner.nextLine().trim().toLowerCase();
                if ("back".equals(action)) {
                    System.out.println();
                    return;
                } else if ("again".equals(action)) {
                    break;
                } else if (action.matches("[0-9+]")) {
                    try {
                        int index = Integer.parseInt(action);
                        Record record = records.get(indexesFound.get(index - 1));
                        System.out.println(record.toString());
                        while (true) {
                            System.out.print("[record] Enter action (edit, delete, menu): ");
                            action = scanner.nextLine().trim().toUpperCase();
                            switch (action) {
                                case "MENU":
                                    System.out.println();
                                    return;
                                case "EDIT":
                                    record.changeFields();
                                    System.out.println(record.toString());
                                    break;
                                case "DELETE":
                                    records.remove(record);
                                    System.out.println("The record deleted!");
                                    System.out.println();
                                    return;
                            }
                        }
                    } catch (Exception e) {
                        System.out.println("Wrong number");
                    }
                } else {
                    System.out.println("Wrong action/number");
                }
            }
        }
    }

    public void count() {
        System.out.println("The Phone Book has " + records.size() + " records.\n");
    }

    private void listRecords() {
        int i = 0;
        for (Record rec : records) {
            i++;
            System.out.println(i + "." + " " + rec.nameList());
        }
        System.out.println();
    }

    private ArrayList<Integer> searching(String searchString) {
        ArrayList<Integer> array = new ArrayList<>();
        Pattern pattern = Pattern.compile(searchString, Pattern.CASE_INSENSITIVE);
        StringBuilder result = new StringBuilder();
        int i = 0;
        for (Record rec : records) {
            if (rec.getName() != null) {
                result.append(rec.getName());
            }
            if (rec.getSurname() != null) {
                result.append(rec.getSurname());
            }
            if (rec.birthDate != null) {
                result.append(rec.birthDate).toString();
            }
            if (rec.getGender() != null) {
                result.append(rec.getGender());
            }
            if (rec.getOrganizationName() != null) {
                result.append(rec.getOrganizationName());
            }
            if (rec.getAddress() != null) {
                result.append(rec.getAddress());
            }
            if (rec.getPhoneNumber() != null) {
                result.append(rec.getPhoneNumber());
            }
            if (rec.getCreationTime() != null) {
                result.append(rec.getCreationTime());
            }
            if (rec.getUpdateTime() != null) {
                result.append(rec.getUpdateTime());
            }

            Matcher matcher = pattern.matcher(result);
            if (matcher.find()) {
                array.add(i);
            }
            i++;
            result = new StringBuilder();
        }

        return array;
    }
}


class ValidNumber {
    static boolean isValidNumber(String phoneNumber) {
        String regex = "(?i)(\\+?\\d?[\\ \\-]?|)(\\([a-z0-9]{2,}\\)[\\ \\-]?|[a-z0-9]{2,}[\\ \\-]\\([a-z0-9]{2,}\\)[\\ \\-]?|)([a-z0-9]{2,}[\\ \\-]?){0,}";
        return phoneNumber.matches(regex);
    }
}

abstract class Record implements Serializable {
    private static final long serialVersionUID = 1L;

    String name;
    String surname;
    LocalDate birthDate;
    String gender;
    String organizationName;
    String address;
    String phoneNumber;
    LocalDateTime creationTime;
    LocalDateTime updateTime;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getOrganizationName() {
        return organizationName;
    }

    public void setOrganizationName(String companyName) {
        this.organizationName = companyName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public LocalDateTime getCreationTime() {
        return creationTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    abstract void changeFields();

    abstract String nameList();

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();

        if (name != null) {
            result.append("Name: ").append(name);
            result.append("\n");
        }
        if (surname != null) {
            result.append("Surname: ").append(surname);
            result.append("\n");
        }
        if (birthDate != null) {
            result.append("Birth date: ").append(birthDate);
            result.append("\n");
        } else {
            result.append("Birth date: ").append("[no data]");
            result.append("\n");
        }
        if (gender != null) {
            result.append("Gender: ").append(gender);
            result.append("\n");
        }
        if (organizationName != null) {
            result.append("Organization name: ").append(organizationName);
            result.append("\n");
        }
        if (address != null) {
            result.append("Address: ").append(address);
            result.append("\n");
        }
        if (phoneNumber != null) {
            result.append("Number: ").append(phoneNumber);
            result.append("\n");
        }
        if (creationTime != null) {
            result.append("Time created: ").append(creationTime.toString().substring(0, 16));
            result.append("\n");
        }
        if (updateTime != null) {
            result.append("Time last edit: ").append(updateTime.toString().substring(0, 16));
        }
        result.append("\n");

        return result.toString();
    }
}

class Person extends Record implements Serializable {
    private static final long serialVersionUID = 1L;

    Person(String name, String surname, LocalDate birthDate, String gender, String phoneNumber, LocalDateTime creationTime, LocalDateTime updateTime) {
        this.name = name;
        this.surname = surname;
        this.birthDate = birthDate;
        this.gender = gender;
        this.phoneNumber = phoneNumber;
        this.creationTime = creationTime;
        this.updateTime = updateTime;
    }

    @Override
    void changeFields() {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Select a field (name, surname, birth, gender, number): ");
        String person = scanner.nextLine().trim().toLowerCase();
        switch (person) {
            case "name":
                System.out.print("Enter name: ");
                String name = scanner.nextLine().trim();
                this.setName(name);
                this.setUpdateTime(LocalDateTime.now());
                break;
            case "surname":
                System.out.print("Enter surname: ");
                String surname = scanner.nextLine().trim();
                this.setSurname(surname);
                this.setUpdateTime(LocalDateTime.now());
                break;
            case "birth":
                System.out.print("Enter birth: ");
                try {
                    LocalDate birth = LocalDate.parse(scanner.nextLine().trim());
                    this.setBirthDate(birth);
                    this.setUpdateTime(LocalDateTime.now());
                } catch (DateTimeParseException exc) {
                    System.out.println("Bad birth date!");
                }
                break;
            case "gender":
                System.out.print("Enter gender: ");
                String gender = scanner.nextLine().trim();
                this.setGender(gender);
                this.setUpdateTime(LocalDateTime.now());
                break;
            case "number":
                System.out.print("Enter number: ");
                String number = scanner.nextLine().trim();
                if (!ValidNumber.isValidNumber(number)) {
                    System.out.println("Wrong number format!");
                    number = "[no number]";
                }
                this.setPhoneNumber(number);
                this.setUpdateTime(LocalDateTime.now());
                break;
        }
        System.out.println("Saved");
    }

    @Override
    String nameList() {
        return getName() + " " + getSurname();
    }

}

class Organization extends Record implements Serializable {
    private static final long serialVersionUID = 1L;

    Organization(String organizationName, String address, String phoneNumber, LocalDateTime createTime, LocalDateTime updateTime) {
        this.organizationName = organizationName;
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.creationTime = createTime;
        this.updateTime = updateTime;
    }

    @Override
    void changeFields() {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Select a field (name, address, number): ");
        String organization = scanner.nextLine().trim().toLowerCase();

        switch (organization) {
            case "name":
                System.out.print("Enter name: ");
                String name = scanner.nextLine().trim();
                this.setOrganizationName(name);
                this.setUpdateTime(LocalDateTime.now());
                break;
            case "address":
                System.out.print("Enter address: ");
                String address = scanner.nextLine().trim();
                this.setAddress(address);
                this.setUpdateTime(LocalDateTime.now());
                break;
            case "number":
                System.out.print("Enter number: ");
                String number = scanner.nextLine().trim();
                if (!ValidNumber.isValidNumber(number)) {
                    System.out.println("Wrong number format!");
                    number = "[no number]";
                }
                this.setPhoneNumber(number);
                this.setUpdateTime(LocalDateTime.now());
                break;
        }
        System.out.println("Saved");
    }

    @Override
    String nameList() {
        return getOrganizationName();
    }

}

class SerializationUtils {

    public static void saveBase(int length, List<Record> records, String fileName) {
        if (length != 0) {
            try {
                serialize(records, fileName);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void serialize(Object obj, String fileName) throws IOException {
        FileOutputStream fos = new FileOutputStream(fileName);
        BufferedOutputStream bos = new BufferedOutputStream(fos);
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(obj);
        oos.close();
    }

    public static Object deserialize(String fileName) throws IOException, ClassNotFoundException {
        FileInputStream fis = new FileInputStream(fileName);
        BufferedInputStream bis = new BufferedInputStream(fis);
        ObjectInputStream ois = new ObjectInputStream(bis);
        Object obj = ois.readObject();
        ois.close();
        return obj;
    }
}