import TcSimExceptions.AmbigiousObjects;
import TcSimExceptions.ExpectedAnObject;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;

public class AnsysWorkbench extends SimulationTool {

    public AnsysWorkbench(LinkedList<String> arguments) {
        super(arguments);
    }

    private Path writeNewProjectPath() throws IOException {
        String version = "25.2.0";
        Path projectPath = Path.of(this.getWorkingDirectory() +"\\" + this.projectName + ".wbpj");
        Path journalPath = Path.of(this.getWorkingDirectory() +"\\NewAnsysProject.wbjn");

        String journalScript = String.format(
            "SetScriptVersion(Version='%s'))" +
            "\nSave(FilePath=r'%s', Overwrite=True)", version, projectPath);

        try (BufferedWriter w = new BufferedWriter(new FileWriter(journalPath.toFile()))) {
            LOG.info("Writing NewAnsysProject.wbjn in the working direcory");
            w.write(journalScript);
        }

        return journalPath;
    }

    protected void postLaunch() throws AmbigiousObjects, ExpectedAnObject, IOException, InterruptedException {
        Path workingDirectory = Path.of(this.getWorkingDirectory());
        List<Path> folders = this.findFolderWithSuffix("_files", workingDirectory);

        int n = folders.size();
        if (n == 0) {
            String message = "Could not locate any objects in " + workingDirectory + " with suffix _files";
            LOG.warning(message);
            throw new ExpectedAnObject(message);
        } else if (n > 1) {
            String message = "There are multiple directories in " + workingDirectory + " with suffix _files";
            LOG.warning(message);
            throw new AmbigiousObjects(message);
        }
        Path resultingArchive;
        if (this.sevenZipExe == null) {
            resultingArchive = workingDirectory.resolve(this.projectName + ".zip");
        } else {
            resultingArchive = workingDirectory.resolve(this.projectName + ".7z");
        }

        this.packArchive(folders, resultingArchive);
    }

    @Override
    public void newProject(){
        Path journal;
        // Pre-Create New Project
        try {
            LOG.info("Starting new project operation. Retrieving the user input project name.");
            String projectName = this.getNewProjectNameInput();
            this.setProjectName(projectName);
            journal = this.writeNewProjectPath();
        } catch (IOException ioe) {
            String message = "Failed to write New Project journal file in the working directory";
            LOG.severe(message);
            PopupManager.showError("IO Failure", message);
            return;
        }

        // Create New Project
        try {
            this.launchTool(List.of("-I", "-R", journal.toString()));

        } catch (IOException | InterruptedException e) {
            String message = "Failed to launch the tool or the launch was interrupted.";
            PopupManager.showError("Launch Failed", message);
            throw new RuntimeException(e);
        }


        // Post Launch
        try {
            this.postLaunch();
        } catch (IOException | InterruptedException e) {
            String message = "Post-launch archiving failed.";
            PopupManager.showError("Post-launch Failed", message);
            throw new RuntimeException(e);
        } catch (AmbigiousObjects e) {
            // ADD LOGIC TO HANDLE THIS USE CASE WITH JAVAFX
        } catch (ExpectedAnObject e) {
            // ADD LOGIC TO HANDLE THIS USE CASE WITH JAVAFX
        }






    }


    @Override
    public void openExisting() {

    }

    @Override
    public void silentSolve() {
    }




}

