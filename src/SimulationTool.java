import java.io.*;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.logging.Logger;
import java.util.regex.*;
import java.util.stream.Stream;
import TcSimExceptions.*;

public class SimulationTool {
    protected static final Logger LOG = Logger.getLogger(SimulationTool.class.getName());
    public String logPath = "..\\sim_process_launch_summary.log";
    protected HashMap<String, String> userInputs;
    protected HashMap<String, String> teamcenterInputs;
    protected Path sevenZipExe;
    protected String projectName;

    protected String toolPath;

    // Constructor
    public SimulationTool(LinkedList<String> arguments) {
        HashMap<String, String> inputs = new LinkedHashMap<>();
        inputs.put("filePath", null);
        inputs.put("workingDirectory", null);
        inputs.put("inputID", null);
        inputs.put("inputRevID", null);
        inputs.put("inputDatasetName", null);
        inputs.put("toolInputArgs", null);
        inputs.put("inputRevKey", null);
        inputs.put("inputXML", null);
        inputs.put("itemCreationSetting", null);
        inputs.put("datasetCreationSetting", null);
        inputs.put("hostPath", null);
        inputs.put("userID", null);
        inputs.put("securityToken", null);
        inputs.put("userGroup", null);
        inputs.put("userStagingDir", null);
        inputs.put("sharedStagingDir", null);
        inputs.put("inputItemKey", null);
        inputs.put("HPCHost", null);
        inputs.put("HPCAuthProtocol", null);
        inputs.put("HPCPublicKeyLoc", null);
        inputs.put("HPCSubmissionCmd", null);
        inputs.put("HPCOtherArgs", null);
        inputs.put("HPCMonitorCmd", null);
        inputs.put("HPCCancelCmd", null);
        inputs.put("HPCSolverName", null);

        for (String parameter : inputs.keySet()) {
            String argument = arguments.pollFirst();
            if (argument == null) break;
            inputs.put(parameter, argument);
        }

        this.teamcenterInputs = inputs;


        if (!inputs.get("toolInputArgs").equalsIgnoreCase("NoValue")) {
            this.userInputs = new HashMap<>();
            Pattern pattern = Pattern.compile("-(?<parameter>\\w+)=(?<argument>.*)");
            String[] toolInputArgs = inputs.get("toolInputArgs").split("\\?");

            for (String arg : toolInputArgs) {
                Matcher matcher = pattern.matcher(arg);
                if (matcher.matches()) {
                    String parameter = matcher.group("parameter");
                    String argument = matcher.group("argument");
                    this.userInputs.put(parameter, argument);
                }
            }
        } else {
            this.userInputs = null;
        }

        String sevenZip = System.getenv("TCSIM_TOOLPATH_SEVENZIP");
        this.sevenZipExe = sevenZip != null ? Path.of(sevenZip) : null;

    }

    /**
     * Launches tool path defined in the SimulationTool constructor along with a list of arguments
     * @param args Passed as a list of strings
     * @throws IOException Thrown if program fails to start properly
     * @throws InterruptedException Thrown with user intervention
     */
    public void launchTool(List<String> args) throws IOException, InterruptedException {
        ArrayList<String> cmd = new ArrayList<>(List.of(toolPath));
        if (args != null) cmd.addAll(args);

        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.inheritIO(); LOG.info("Executing: " + cmd);
        int status=pb.start().waitFor();

        LOG.info(getClass().getSimpleName()+" exited with code "+status);

        if (status != 0) {
            PopupManager.showWarning("Tool Completed Unsuccessfully", "The tool returned error code: " + status);
        }

    }

    // File Operation
    // ----------------------------------------------------------------------------------------------------------------
    /**
     * Iterates through working directory to return a list of file Path instances
     * @param prefix passed as a String
     * @param suffix passed as a String
     * @return An ArrayList<Path> fitting the regex criteria "prefix(.*)suffix"
     */
    public List<Path> findFilesWith(String prefix, String suffix) throws IOException {
        Pattern pattern;
        if (suffix != null && prefix != null) {
            pattern = Pattern.compile("^" + prefix + "(.*)" + suffix + "$");
        } else if (suffix == null && prefix == null) {
            pattern = Pattern.compile("(.*)");
        } else if (suffix == null) {
            pattern = Pattern.compile("^" + prefix + "(.*)");
        } else {
            pattern = Pattern.compile("(.*)" + suffix + "$");
        }

        LOG.info("Using pattern: " + pattern);

        ArrayList<Path> paths = new ArrayList<>(2);
        Path workingDirectory = Path.of(this.getWorkingDirectory());

        LOG.info("Iterating through working directory: " + workingDirectory);
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(workingDirectory)) {
            for (Path entry : stream) {
                boolean isFile = new File(entry.toUri()).isFile();
                if (!isFile) {
                    continue;
                }
                String name = entry.getFileName().toString();
                LOG.info("Found: " + name);
                Matcher matcher = pattern.matcher(name);
                if (matcher.matches()) {
                    LOG.info(name + " fits the pattern criteria. Adding to return list.") ;
                    paths.add(entry);
                }
            }
        }
        return paths;
    }

    /**
     * Iterates through working directory to return a list of Path instances
     * @param prefix passed as a String
     * @return An ArrayList<Path> fitting the regex criteria "prefix(.*)suffix"
     */
    public List<Path> findFilesWithPrefix(String prefix) throws IOException {
        return this.findFilesWith(prefix, null);
    }

    /**
     * Iterates through working directory to return a list of Path instances
     * @param suffix passed as a String
     * @return An ArrayList<Path> fitting the regex criteria "prefix(.*)suffix"
     */
    public List<Path> findFilesWithSuffix(String suffix) throws IOException {
        return this.findFilesWith(null, suffix);
    }

    /**
     * Deletes files with a specified prefix and suffix. A directory to iterate through must be provided.
     * @param prefix Specified prefix passed as a String
     * @param suffix Specified suffix passed as a String
     * @param directory Specified directory to iterate through, passed as a Path
     */
    public void deleteFilesWith(String prefix, String suffix, Path directory) throws IOException {
        Pattern pattern;
        if (suffix != null && prefix != null) {
            pattern = Pattern.compile("^" + prefix + "(.*)" + suffix + "$");
        } else if (suffix == null && prefix == null) {
            pattern = Pattern.compile("(.*)");
        } else if (suffix == null) {
            pattern = Pattern.compile("^" + prefix + "(.*)");
        } else {
            pattern = Pattern.compile("(.*)" + suffix + "$");
        }

        LOG.info("Using pattern: " + pattern);
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(directory)) {
            LOG.info("Iterating through working directory: " + directory);

            for (Path path : stream) {
                Matcher matcher = pattern.matcher(path.getFileName().toString());
                File file  = new File(path.toUri());
                if (!(file.isFile() && matcher.matches())) continue;
                LOG.info("Deleting " + path);
                Files.delete(path);
                
            }
        }
    }

    /**
     * Deletes files with a specified prefix. A directory to iterate through must be provided.
     * @param prefix Specified prefix passed as a String
     * @param directory Specified directory to iterate through, passed as a Path
     */
    public void deleteFilesWithPrefix(String prefix, Path directory) throws IOException {
        this.deleteFilesWith(prefix, null, directory);
    }

    /**
     * Deletes files with a specified prefix. A directory to iterate through must be provided.
     * @param suffix Specified suffix passed as a String
     * @param directory Specified directory to iterate through, passed as a Path
     */
    public void deleteFilesWithSuffix(String suffix, Path directory) throws IOException {
        this.deleteFilesWith(null, suffix, directory);
    }

    /**
     * Copies the sim_launch_process.log to the working directory to prepare for import to Teamcenter
     */
    protected void copyLogFile2WorkingDirectory() throws IOException {

        Path logPath = Path.of(this.logPath);
        String filename = logPath.getFileName().toString();
        Path destinationPath = Path.of(this.getWorkingDirectory() + "\\" + filename);

        // REPLACE_EXISTING overwrites the target file if it already exists
        LOG.info("Attempting to copy " + logPath + " to " + destinationPath + "...");
        Files.copy(logPath, destinationPath, StandardCopyOption.REPLACE_EXISTING);
        LOG.info("File copied successfully.");
    }

    // Folder Operations
    // ----------------------------------------------------------------------------------------------------------------
    /**
     * Iterates through working directory to return a list of folder Path instances
     * @param prefix passed as a String
     * @param suffix passed as a String
     * @return An ArrayList<Path> fitting the regex criteria "prefix(.*)suffix"
     */
    public List<Path> findFolderWith(String prefix,String suffix) throws IOException {

        Pattern pattern;
        if (suffix != null && prefix != null) {
            pattern = Pattern.compile("^" + prefix + "(.*)" + suffix + "$");
        } else if (suffix == null && prefix == null) {
            pattern = Pattern.compile("(.*)");
        } else if (suffix == null) {
            pattern = Pattern.compile("^" + prefix + "(.*)");
        } else {
            pattern = Pattern.compile("(.*)" + suffix + "$");
        }

        LOG.info("Using pattern: " + pattern);

        ArrayList<Path> paths = new ArrayList<>(2);
        Path workingDirectory = Path.of(this.getWorkingDirectory());

        LOG.info("Iterating through working directory: " + this.getWorkingDirectory());
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(workingDirectory)) {
            for (Path entry : stream) {
                boolean isDirectory = new File(entry.toUri()).isDirectory();
                if (!isDirectory) {
                    continue;
                }
                String name = entry.getFileName().toString();
                LOG.info("Found: " + name);
                Matcher matcher = pattern.matcher(name);
                if (matcher.matches()) {
                    LOG.info(name + " fits the pattern criteria. Adding to return list.");
                    paths.add(entry);
                }
            }
        }
        return paths;
    }

    /**
     * Iterates through working directory to return a list of folder Path instances
     * @param prefix passed as a String
     * @return An ArrayList<Path> fitting the regex criteria "prefix(.*)suffix"
     */
    public List<Path> findFolderWithPrefix(String prefix) throws IOException {
        return findFolderWith(prefix, null);
    }

    /**
     * Iterates through working directory to return a list of folder Path instances
     * @param suffix passed as a String
     * @return An ArrayList<Path> fitting the regex criteria "prefix(.*)suffix"
     */
    public List<Path> findFolderWithSuffix(String suffix) throws IOException {
        return findFolderWith(null, suffix);
    }

    /**
     * Deletes folders with a specified keyword in a directory
     * @param keyword Passed as a String
     * @param directory Passed as a Path instance
     * @throws IOException Thrown if
     */
    public void deleteFoldersWithKeyword(String keyword, Path directory) throws IOException {
        Pattern pattern = Pattern.compile("^(.*)" + keyword + "(.*)$");

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(directory)) {
            for (Path path : stream) {
                Matcher matcher = pattern.matcher(path.getFileName().toString());
                File file = new File(path.toUri());
                if (!(file.isDirectory() && matcher.matches())) continue;
                this.deleteDirectory(path);
            }
        }
    }


    private void deleteDirectory(Path directory) throws IOException {
        if (!Files.exists(directory)) {
            return;
        }

        try (Stream<Path> paths = Files.walk(directory)) {
            for (Path path : paths.sorted(Comparator.reverseOrder()).toList()) {
                Files.delete(path);
            }
        }

        LOG.info("Deleted directory: " + directory);
    }


    /**
     * Opens the Windows File Explorer in the working directory
     * @throws IOException Thrown if Windows explorer fails to launch
     */
    public void openExplorerInWD() throws IOException {
        new ProcessBuilder("explorer.exe", Path.of(this.getWorkingDirectory()).toAbsolutePath().toString()).start();
    }

    // Archive Operations
    // ----------------------------------------------------------------------------------------------------------------

    /**
     * Extracts a 7z or zip to a specified directory. May throw an IOException.
     * @param archive_path specified path for a zip or 7z
     * @param output_directory specified output directory for extracted contents
     * @throws InterruptedException Thrown with user intervention
     * @throws IOException Thrown if the exit code from 7z.exe or tar is not 0
     */
    public void extractArchive(Path archive_path, Path output_directory) throws InterruptedException, IOException {
        Process process;
        ProcessBuilder pb;

        if (sevenZipExe != null) {
            LOG.info("Using 7z");
            LOG.info("Extracting archive in " + output_directory + "...");
            pb = new ProcessBuilder(this.sevenZipExe.toString(), "x", "-y", archive_path.toAbsolutePath().toString(),
                    "-o"+ output_directory.toAbsolutePath());


        } else {
            LOG.info("Using tar");
            LOG.info("Extracting archive in " + output_directory + "...");
            pb = new ProcessBuilder("tar", "-xf", archive_path.toAbsolutePath().toString(), "-C" + "\"" + output_directory.toAbsolutePath() + "\"");

        }
        process = pb.start();
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new IOException("Failed to extract: " + archive_path.toAbsolutePath() + " to " +
                    output_directory.toAbsolutePath());
        }
        LOG.info("Extract Complete!");

    }

    /**
     * Extracts a 7z or zip to a specified directory. May throw an IOException.
     * @param object_paths List of file or folder paths to be packed in a 7z or zip
     * @param resulting_archive_path specified output archive for packed contents
     * @throws InterruptedException Thrown with user intervention
     * @throws IOException Thrown if the exit code from 7z.exe or tar is not 0
     */
    public void packArchive(List<Path> object_paths, Path resulting_archive_path) throws InterruptedException, IOException {
        Process process;
        ProcessBuilder pb;

        if (sevenZipExe != null) {
            LOG.info("Using 7z");
            LOG.info("Packing archive in " + resulting_archive_path + "...");
            pb = new ProcessBuilder(this.sevenZipExe.toString(), "a", "-y", resulting_archive_path.toAbsolutePath().toString());
            for (Path path : object_paths) {
                pb.command().add(path.toAbsolutePath().toString());
            }
        } else {
            LOG.info("Using tar");
            LOG.info("Packing archive in " + resulting_archive_path + "...");
            pb = new ProcessBuilder("tar", "-cf", resulting_archive_path.toAbsolutePath().toString());
            for (Path path : object_paths) {
                pb.command().add(path.toAbsolutePath().toString());
            }
        }

        process = pb.start();
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new IOException("Failed to pack: " + resulting_archive_path.toAbsolutePath() + " to " +
                    resulting_archive_path.toAbsolutePath());
        }
        LOG.info("Packing Complete!");
    }




    // MAIN ACTIONS - OVERWRITTEN IN CHILD CLASSES
    // ================================================================================================================
    public void newProject() {}
    public void openExisting() {}
    public void solveSilently() {}

    // GETTERS
    // ================================================================================================================
    protected String getFilePath() {
        return this.teamcenterInputs.get("filePath");
    }

    protected String getWorkingDirectory() {
        return this.teamcenterInputs.get("workingDirectory");
    }

    protected String getInputID() {
        return this.teamcenterInputs.get("inputID");
    }

    protected String getInputRevID() {
        return this.teamcenterInputs.get("inputRevID");
    }

    protected String getInputDatasetName() {
        return this.teamcenterInputs.get("inputDatasetName");
    }

    protected String getProjectNameFromPath() throws NameParsingError {
        Path path = Path.of(this.getFilePath());
        String filename = path.getFileName().toString();
        Pattern pattern = Pattern.compile("^(?<root>.+)\\.(.+)?");
        Matcher matcher = pattern.matcher(filename);

        if (matcher.matches()) {
            String root = matcher.group("root");
            LOG.info("Retrieved "  +root + " from parsing the file");
            return root;
        }

        LOG.warning("Could not extract project name from file path.");
        throw new NameParsingError("Could not extract project name from file path.");
    }

    protected String getNewProjectNameInput() {

        LOG.info("Retrieving user input ProjectName...");
        String name = this.userInputs.get("ProjectName");
        if (name != null) {
            LOG.info("Retrived " + name);
            if (name.contains(" ")) {
                LOG.info("Replacing whitespace by underscores...");
                name = name.replace(" ", "_");
            }
            return name;
        }

        LOG.warning("Could not gather new project name from ProjectName user input. Defaulting to ID_RevID_ToolName.");
        return String.format(this.getInputID()+"_"+this.getInputRevID()+"_"+getClass().getSimpleName());
    }


    // SETTERS
    // ================================================================================================================
    public void setLogPath(String logPath) {
        this.logPath = logPath;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }
}