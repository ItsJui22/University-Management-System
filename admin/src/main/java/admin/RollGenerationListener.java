package admin;

public interface RollGenerationListener {
    void onRollsGenerated();

    void setGeneratedRoll(long generatedRoll);
}
