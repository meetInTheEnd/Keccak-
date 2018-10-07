package zzzqi.keccak;

import android.app.Activity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;

public class AndroidMonitor extends Activity {

    private static String PID(String PackageName) {

        Process proc = null;
        String str3 = null;
        try {
            Runtime runtime = Runtime.getRuntime();
            System.out.println(runtime.exec("adb shell ps "));
            proc = runtime.exec("adb shell ps |grep  " + PackageName);

            if (proc.waitFor() != 0) {
                System.err.println("exit value = " + proc.exitValue());
            }
            BufferedReader in = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            StringBuffer stringBuffer = new StringBuffer();
            String line = null;
            while ((line = in.readLine()) != null) {
                stringBuffer.append(line + " ");

            }
            String str1 = stringBuffer.toString();
            String str2 = str1.substring(str1.indexOf(" " + PackageName) - 46, str1.indexOf(" " + PackageName));
            String PID = str2.substring(0, 7);
            PID = PID.trim();

            str3 = PID;
        } catch (Exception e) {
            System.err.println(e);
        } finally {
            try {
                proc.destroy();
            } catch (Exception e2) {
            }
        }

        return str3;
    }

    public static double getFlow(String PackageName) {

        double flow = 0;
        try {

            String Pid = PID(PackageName);

            Runtime runtime = Runtime.getRuntime();
            Process proc = runtime.exec("adb shell cat /proc/" + Pid + "/net/dev");
            try {
                if (proc.waitFor() != 0) {
                    System.err.println("exit value = " + proc.exitValue());
                }
                BufferedReader in = new BufferedReader(new InputStreamReader(proc.getInputStream()));
                StringBuffer stringBuffer = new StringBuffer();
                String line = null;
                while ((line = in.readLine()) != null) {
                    stringBuffer.append(line + " ");

                }
                String str1 = stringBuffer.toString();
                String str2 = str1.substring(str1.indexOf("wlan0:"), str1.indexOf("wlan0:") + 90);
                String str4 = str2.substring(7, 16);
                str4 = str4.trim();
                String str6 = str2.substring(67, 75);
                str6 = str6.trim();
                int b = Integer.parseInt(str4);
                int a = Integer.parseInt(str6);

                double sendFlow = a / 1024;
                double revFlow = b / 1024;
                flow = sendFlow + revFlow;

            } catch (InterruptedException e) {
                System.err.println(e);
            } finally {
                try {
                    proc.destroy();
                } catch (Exception e2) {
                }
            }
        } catch (Exception StringIndexOutOfBoundsException) {
            System.out.println("请检查设备是否连接");

        }

        return flow;
    }

    public static String getCPU(String PackageName) {

        double Cpu = 0;
        String str1=null;
        try {

            Runtime runtime = Runtime.getRuntime();
            Process proc = runtime.exec("top -n 1|grep" + PackageName);
            try {
                if (proc.waitFor() != 0) {
                    System.err.println("exit value = " + proc.exitValue());
                }
                BufferedReader in = new BufferedReader(new InputStreamReader(proc.getInputStream()));
                StringBuffer stringBuffer = new StringBuffer();
                String line = null;
                while ((line = in.readLine()) != null) {
                    stringBuffer.append(line + " ");

                }

                str1 = stringBuffer.toString();
                System.out.print(str1);
                /**String str3 = str1.substring(str1.indexOf(PackageName) - 43, str1.indexOf(PackageName)).trim();
                String cpu = str3.substring(0, 2);
                cpu = cpu.trim();
                Cpu = Double.parseDouble(cpu);
                 **/

            } catch (InterruptedException e) {
                System.err.println(e);
            } finally {
                try {
                    proc.destroy();
                } catch (Exception e2) {
                }
            }
        } catch (Exception StringIndexOutOfBoundsException) {

            StringIndexOutOfBoundsException.printStackTrace();

        }

        return str1;

    }

    public static String getMemory(String PackageName) {

        double Heap = 0;
        String str1= null;

        try {
            Runtime runtime = Runtime.getRuntime();
            Process proc = runtime.exec("ls ");
            try {
                if (proc.waitFor() != 0) {
                    System.err.println("exit value = " + proc.exitValue());
                }
                BufferedReader in = new BufferedReader(new InputStreamReader(proc.getInputStream()));
                StringBuffer stringBuffer = new StringBuffer();
                String line = null;
                while ((line = in.readLine()) != null) {
                    stringBuffer.append(line + " ");

                }

                str1 = stringBuffer.toString();
                System.out.print(str1);
                /**String str2 = str1.substring(str1.indexOf("Objects") - 60, str1.indexOf("Objects"));
                String str3 = str2.substring(0, 10);
                str3 = str3.trim();
                Heap = Double.parseDouble(str3) / 1024;
                DecimalFormat df = new DecimalFormat("#.000");
                String memory = df.format(Heap);
                Heap = Double.parseDouble(memory);
                 **/

            } catch (InterruptedException e) {
                System.err.println(e);
            } finally {
                try {
                    proc.destroy();
                } catch (Exception e2) {
                }
            }
        }

        catch (Exception StringIndexOutOfBoundsException) {
            StringIndexOutOfBoundsException.printStackTrace();

        }
        return str1;
    }
}
