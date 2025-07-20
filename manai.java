import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
class manai 
{
    Scanner sc = new Scanner(System.in);
    double alph = 0.5, bet = 0.5;
    int mrkvsuc = 0, rwpsuc = 0, nocm = 0, nocp = 0;
    double simrw[] = new double[10];        // Frequency counts
    double simpro[] = new double[10];       // Frequency probabilities
    double arr[][] = new double[10][10];    // Markov probabilities
    double arrw[][] = new double[10][10];   // Markov weights
    File file;
    manai() 
    {
        Random rand = new Random();
        for (int i = 0; i < 10; i++) 
        {
            simpro[i] = 1.0 / 10; // Uniform initialization
            for (int j = 0; j < 10; j++) 
            {
                arr[i][j] = 1.0 / 10 + rand.nextDouble() * 0.01; // Slight randomness
            }
        }
    }

    void filehandler(String name) 
    {
        file = new File(name);
        try 
        {
            if (!file.exists()) 
            {
                System.out.println("New user detected. Creating a new file...");
                file.createNewFile();
            }
            else 
            {
                System.out.println("File exists. Reading and analyzing previous data...");
                filereader();
            }
        } 
        catch (IOException e) 
        {
            System.out.println("Error: " + e.getMessage());
        }
    }

    void filereader() 
    {
        int prev = -1;
        try (Scanner sc = new Scanner(file)) 
        {
            while (sc.hasNextInt()) 
            {
                int number = sc.nextInt();
                if (number < 0 || number > 9) continue;
                rawprob(number);
                bayesstep();
                if (prev != -1) markovchn(prev, number);
                prev = number;
            }
        } 
        catch (IOException e) 
        {
            System.out.println("Error while reading file: " + e.getMessage());
        }
    }

    void filewriter(int val) 
    {
        try (FileWriter writer = new FileWriter(file, true)) 
        {
            writer.write(val + " ");
        } 
        catch (IOException e) 
        {
            System.out.println("Error: " + e.getMessage());
        }
    }

    void input() 
    {
        while (true) 
        {
            System.out.println("Manual (M), Automated (A), or Generate Large Data (G)? Type 'end' to stop:");
            String choice = sc.nextLine();
            if (choice.equalsIgnoreCase("end")) break;
            switch (choice.toUpperCase()) 
            {
                case "M": maninp(); break;
                case "A": atinp(); break;
                case "G": generatelargedata(); break;
                default: System.out.println("Invalid choice! Try again."); break;
            }
        }
    }

    void maninp() 
    {
        int prevInput = -1;
        while (true) 
        {
            int predicted = predicter(prevInput);
            System.out.println("Predicted value: " + predicted);
            System.out.print("Enter input (0-9 or 'end' to stop): ");
            String input = sc.nextLine();
            if (input.equals("end")) break;
            try 
            {
                int val = Integer.parseInt(input);
                if (val < 0 || val > 9) 
                {
                    System.out.println("Invalid input! Enter a value between 0 and 9.");
                    continue;
                }
                if (val == predicted) rwpsuc++;
                filewriter(val);
                rawprob(val);
                bayesstep();
                if (prevInput != -1) markovchn(prevInput, val);
                prevInput = val;
            } 
            catch (NumberFormatException e) 
            {
                System.out.println("Invalid input! Please enter a valid number.");
            }
        }
    }

    void atinp() 
    {
        int prevInput = -1, correctPredictions = 0;
        System.out.println("How many numbers to generate?");
        int size = sc.nextInt();
        sc.nextLine();
        Random rand = new Random();
        for (int i = 0; i < size; i++) 
        {
            int val = rand.nextInt(10); // Random number between 0-9
            int predicted = predicter(prevInput);
            if (val == predicted) correctPredictions++;
            filewriter(val);
            rawprob(val);
            bayesstep();
            if (prevInput != -1) markovchn(prevInput, val);
            prevInput = val;
        }
        System.out.println("Automated input complete. Accuracy: " + (correctPredictions * 100.0 / size) + "%");
    }

    void generatelargedata() 
    {
        System.out.println("How many random numbers to generate (large dataset)?");
        int size = sc.nextInt();
        sc.nextLine();
        Random rand = new Random();
        try (FileWriter writer = new FileWriter(file, true)) 
        {
            for (int i = 0; i < size; i++) 
            {
                int val = rand.nextInt(10);
                writer.write(val + " ");
            }
            System.out.println(size + " random numbers have been generated and added to the file.");
        } 
        catch (IOException e) 
        {
            System.out.println("Error while writing large dataset: " + e.getMessage());
        }
        filereader(); // Analyze the newly generated data
    }

    int predicter(int lastInput) {
        if (lastInput == -1) {
            return getMaxIndex(simpro);
        } else {
            return getMaxIndex(arr[lastInput]);
        }
    }

    int getMaxIndex(double[] array) {
        double max = -1;
        int index = 0;
        for (int i = 0; i < array.length; i++) {
            if (array[i] > max) {
                max = array[i];
                index = i;
            }
        }
        return index;
    }

    void rawprob(int number) {
        nocp++;
        simrw[number]++;
        for (int i = 0; i < 10; i++) {
            simpro[i] = (simrw[i] + 1) / (nocp + 10); // Smooth probabilities
        }
    }

    void markovchn(int a, int b) {
        nocm++;
        arrw[a][b]++;
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                arr[i][j] = (arrw[i][j] + 1) / (nocm + 10); // Smooth Markov probabilities
            }
        }
    }

    void bayesstep() {
        double freqSum = 0, markovSum = 0;
        for (double prob : simpro) freqSum += prob;
        for (double[] row : arr) for (double val : row) markovSum += val;

        alph = freqSum / (freqSum + markovSum);
        bet = markovSum / (freqSum + markovSum);
    }

    public static void main(String[] args) {
        manai ob = new manai();
        System.out.println("Enter your name:");
        String name = ob.sc.nextLine();
        ob.filehandler(name);

        System.out.println("Program ready! Start entering data...");
        ob.input();
        System.out.println("Session complete. Total prediction successes: " + ob.rwpsuc);
    }
}