import java.util.*;

public class MayanLaunch {
    public static void main(String[] args) {

//        SimulationTool simTool = new SimulationTool(new LinkedList<>(Arrays.asList("C:\\Users\\sebastianc\\AppData\\Local\\TEMP\\000092_A_26_GasTurbineCopy\\000092_A.sim", "C:\\Users\\SEBAST~2\\AppData\\Local\\Temp\\ESP_Root_1794847061663\\ESP_1794847061674", "000092", "A", "000092-A", "-TCSIM_TOOLPATH_EXECUTABLE=", "QgHAAAyN5rQd$B", "cae1794847059", "As_Needed", "As_Needed", "http://playground:3000/tc/", "null", "null", "null", "C:\\Users\\sebastianc\\AppData\\Local\\TEMP\\000092_A_26_GasTurbineCopy", "C:\\Users\\SEBAST~1\\AppData\\Local\\Temp\\000092_A_26_GasTurbineCopy", "QEHAAAyN5rQd$B")));

        LinkedList<String> arguments = new LinkedList<>(Arrays.asList(args));
        String tool = arguments.pollFirst();
        String action = arguments.pollFirst();

        if (action == null || tool == null) {
            System.out.println("Could not extract a value for the action or tool.");
            System.out.println("Recall the script syntax must be in the form %JAVA_HOME% MayanLaunch <TOOL> <ACTION> %*");
            return;
        }

        // IN THE FORM <TOOL> <ACTION> <Launch Specific Arguments>

        SimulationTool simulationTool = switch (tool.strip().toUpperCase()) {
            case "SIMCENTER" -> {yield new SimCenter(arguments);}
            case "ANSYSWB" -> {yield new AnsysWorkbench(arguments);}
            default -> {PopupManager.showError("Unsupported Simulation Tool", "The provided tool " + tool +
                    " is not supported by the MayanLaunch, and failed at initialization. Valid tools include:" +
                    "\n-SimCenter" +
                    "\n-HyperLynx" +
                    "\n-FloEFD" +
                    "\n-AnsysWB" +
                    "\n-AnsysEDT");
                System.out.println("Recall the script syntax must be in the form %JAVA_HOME% MayanLaunch <TOOL> <ACTION> %*");
                yield null;

            }
        };

        if (simulationTool == null) {
            return;
        }

        switch (action.strip().toUpperCase()) {
            case "NEWPROJECT" -> {
                simulationTool.newProject();
            } case "OPENEXISTING" -> {
                simulationTool.openExisting();
            } case "SILENTSOLVE" -> {
                simulationTool.silentSolve();
            }
            default -> {
                PopupManager.showError("Unsupported Action", "The action, " + action + ", is not a supported operation." +
                " Supported arguments include: "+
                "\n\t-NewProject" +
                "\n\t-OpenExisting" +
                "\n\t-SilentSolve");
                System.out.println("Recall the script syntax must be in the form %JAVA_HOME% MayanLaunch <TOOL> <ACTION> %*");
            }
        }

        PopupManager.endJavaFX();
    }
}