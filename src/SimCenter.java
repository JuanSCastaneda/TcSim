import TcSimExceptions.AmbigiousObjects;
import TcSimExceptions.ExpectedAnObject;

import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;

public class SimCenter extends SimulationTool {

    public SimCenter(LinkedList<String> arguments) {
        super(arguments);
    }

    @Override
    public void newProject(){
    }


    @Override
    public void openExisting() {

    }

    @Override
    public void silentSolve() {
    }
}
