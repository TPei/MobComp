package fhfl.voip;

/**
 * Stellt die Methode für den listener von {@link JsonRequest} bereit
 */
public interface AsyncTaskCompleted {
	/**
	 * Wird mit dem Ergebnis von {@link JsonRequest} aufgerufen
	 * @param result
	 */
	void onTaskCompleted(String result);
}
