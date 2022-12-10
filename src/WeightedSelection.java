import java.util.Scanner;

public class WeightedSelection {
    public int[] algorithm(int[] s, int[] f, int[] w) {
        int[] sum = new int[s.length];

        // initialize sum array with correspondent weights of each activity
        for(int i=0; i<s.length; i++) {
            sum[i] = w[i];
        }
        // iterate over the array in a way similar to that of partitioning but instead iterate over sorted finish times to reduce complexity
        for(int i=1; i<s.length; i++) {
            for(int j= 0; j<i; j++) {
                // compatible activities
                if(s[i] >= f[j])
                    sum[i] = Math.max(sum[i], w[i] + sum[j]);
            }
        }
        return sum;
    }

    public int[] sort(int[] arr, int[] f) {
        for (int i = 0; i < f.length; i++) {
            for (int j = i + 1; j < f.length; j++) {
                int temp;
                if (f[i] > f[j]) {
                    temp = arr[i];
                    arr[i] = arr[j];
                    arr[j] = temp;
                }
            }
        }
        return arr;
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        int n = 3;
        int[] s = {2,1,3};//new int[n];
        int[] f = {3,2,4};//new int[n];
        int[] w = {1,2,5};//new int[n];
/*
        for(int i=0; i<n; i++) {
            s[i] = scanner.nextInt();
            f[i] = scanner.nextInt();
            w[i] = scanner.nextInt();
        }

 */
        // sort the activities w.r.t their finish time (earliest finish time)
        WeightedSelection weightedSelection = new WeightedSelection();
        s = weightedSelection.sort(s, f);
        f = weightedSelection.sort(f, f);
        w = weightedSelection.sort(w, f);

        int[] sum = weightedSelection.algorithm(s, f, w);
        // find max activity sum
        int max = 0;
        for(int i=0; i<sum.length; i++) {
            max = Math.max(max, sum[i]);
        }
        System.out.println(max);
    }
}
