import java.util.*;
import java.util.stream.Collectors;

// Candidate Class
class Candidate {
    String name;
    double matricScore;
    double fscScore;
    boolean feePaid;
    double testScore;
    double interviewScore;

    Candidate(String name, double matricScore, double fscScore) {
        this.name = name;
        this.matricScore = matricScore;
        this.fscScore = fscScore;
        this.feePaid = false;
        this.testScore = 0;
        this.interviewScore = 0;
    }

    @Override
    public String toString() {
        return name + " [Matric: " + matricScore + ", FSC: " + fscScore + "]";
    }
}

// Observer Interface
interface Observer {
    void notify(String message, Candidate candidate);
}

// EmailNotifier Class
class EmailNotifier implements Observer {
    @Override
    public void notify(String message, Candidate candidate) {
        System.out.println("Notification for " + candidate.name + ": " + message);
    }
}

// Filter Interface
interface Filter {
    List<Candidate> apply(List<Candidate> candidates);
}

// Filters Implementation
class EligibilityFilter implements Filter {
    @Override
    public List<Candidate> apply(List<Candidate> candidates) {
        return candidates.stream()
                .filter(c -> c.matricScore >= 60 && c.fscScore >= 60)
                .collect(Collectors.toList());
    }
}

class TestEligibilityFilter implements Filter {
    @Override
    public List<Candidate> apply(List<Candidate> candidates) {
        return candidates.stream()
                .filter(c -> c.feePaid)
                .collect(Collectors.toList());
    }
}

class InterviewEligibilityFilter implements Filter {
    @Override
    public List<Candidate> apply(List<Candidate> candidates) {
        return candidates.stream()
                .filter(c -> c.testScore > 85)
                .collect(Collectors.toList());
    }
}

class MeritListFilter implements Filter {
    @Override
    public List<Candidate> apply(List<Candidate> candidates) {
        return candidates.stream()
                .sorted((c1, c2) -> Double.compare(c2.interviewScore, c1.interviewScore))
                .limit(3)
                .collect(Collectors.toList());
    }
}

// Admission Pipeline
class AdmissionPipeline {
    private final List<Filter> filters = new ArrayList<>();
    private final List<Observer> observers = new ArrayList<>();

    public void addFilter(Filter filter) {
        filters.add(filter);
    }

    public void addObserver(Observer observer) {
        observers.add(observer);
    }

    public List<Candidate> process(List<Candidate> candidates) {
        for (Filter filter : filters) {
            candidates = filter.apply(candidates);
            for (Candidate candidate : candidates) {
                notifyObservers("Processed through " + filter.getClass().getSimpleName(), candidate);
            }
        }
        return candidates;
    }

    private void notifyObservers(String message, Candidate candidate) {
        for (Observer observer : observers) {
            observer.notify(message, candidate);
        }
    }
}

// Main Class
public class AdmissionSystem {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        List<Candidate> candidates = new ArrayList<>();

        // Step 1: Collect 20 users' data
        System.out.println("Enter data for 20 students (name, matric marks, FSC marks):");
        for (int i = 0; i < 20; i++) {
            System.out.print("Name: ");
            String name = scanner.nextLine();
            System.out.print("Matric Marks: ");
            double matricMarks = scanner.nextDouble();
            System.out.print("FSC Marks: ");
            double fscMarks = scanner.nextDouble();
            scanner.nextLine(); // Consume newline
            candidates.add(new Candidate(name, matricMarks, fscMarks));
        }

        // Step 2: Eligibility Filter
        AdmissionPipeline pipeline = new AdmissionPipeline();
        pipeline.addFilter(new EligibilityFilter());
        pipeline.addObserver(new EmailNotifier());

        System.out.println("\nFiltering students based on eligibility...");
        List<Candidate> eligibleCandidates = pipeline.process(candidates);
        System.out.println("Eligible students:");
        eligibleCandidates.forEach(System.out::println);

        // Step 3: Test Fee Submission
        System.out.println("\nEnter fee submission status for eligible students:");
        for (Candidate candidate : eligibleCandidates) {
            System.out.print(candidate.name + " (Fee Paid? true/false): ");
            candidate.feePaid = scanner.nextBoolean();
        }
        scanner.nextLine(); // Consume newline

        pipeline.addFilter(new TestEligibilityFilter());
        System.out.println("\nFiltering students who submitted fee...");
        List<Candidate> feeSubmittedCandidates = pipeline.process(eligibleCandidates);
        System.out.println("Students who submitted the fee:");
        feeSubmittedCandidates.forEach(System.out::println);

        // Step 4: Test Results
        System.out.println("\nEnter test scores for students who submitted fee:");
        for (Candidate candidate : feeSubmittedCandidates) {
            System.out.print(candidate.name + " (Test Score): ");
            candidate.testScore = scanner.nextDouble();
        }
        scanner.nextLine(); // Consume newline

        pipeline.addFilter(new InterviewEligibilityFilter());
        System.out.println("\nFiltering students who passed the test...");
        List<Candidate> interviewCandidates = pipeline.process(feeSubmittedCandidates);
        System.out.println("Students eligible for the interview:");
        interviewCandidates.forEach(System.out::println);

        // Step 5: Interview Results
        System.out.println("\nEnter interview scores for students eligible for interview:");
        for (Candidate candidate : interviewCandidates) {
            System.out.print(candidate.name + " (Interview Score): ");
            candidate.interviewScore = scanner.nextDouble();
        }
        scanner.nextLine(); // Consume newline

        pipeline.addFilter(new MeritListFilter());
        System.out.println("\nSelecting top 3 students based on interview scores...");
        List<Candidate> meritList = pipeline.process(interviewCandidates);
        System.out.println("Merit List:");
        meritList.forEach(System.out::println);

        scanner.close();
    }
}
