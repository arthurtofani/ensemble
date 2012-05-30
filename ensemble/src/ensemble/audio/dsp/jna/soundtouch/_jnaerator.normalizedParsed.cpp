extern ""C"" {
typedef void* HANDLE;
	/**
	 * Create a new instance of SoundTouch processor.<br>
	 * Original signature : <code>HANDLE soundtouch_createInstance()</code>
	 */
	__attribute__((dllimport)) __stdcall HANDLE soundtouch_createInstance();
	/**
	 * Destroys a SoundTouch processor instance.<br>
	 * Original signature : <code>void soundtouch_destroyInstance(HANDLE)</code>
	 */
	__attribute__((dllimport)) __stdcall void soundtouch_destroyInstance(HANDLE h);
	/**
	 * Get SoundTouch library version string<br>
	 * Original signature : <code>char* soundtouch_getVersionString()</code>
	 */
	__attribute__((dllimport)) const __stdcall char* soundtouch_getVersionString();
	/**
	 * environments that can't properly handle character string as return value<br>
	 * Original signature : <code>void soundtouch_getVersionString2(char*, int)</code>
	 */
	__attribute__((dllimport)) __stdcall void soundtouch_getVersionString2(char* versionString, int bufferSize);
	/**
	 * Get SoundTouch library version Id<br>
	 * Original signature : <code>int soundtouch_getVersionId()</code>
	 */
	__attribute__((dllimport)) unsigned __stdcall int soundtouch_getVersionId();
	/**
	 * represent slower rate, larger faster rates.<br>
	 * Original signature : <code>void soundtouch_setRate(HANDLE, float)</code>
	 */
	__attribute__((dllimport)) __stdcall void soundtouch_setRate(HANDLE h, float newRate);
	/**
	 * represent slower tempo, larger faster tempo.<br>
	 * Original signature : <code>void soundtouch_setTempo(HANDLE, float)</code>
	 */
	__attribute__((dllimport)) __stdcall void soundtouch_setTempo(HANDLE h, float newTempo);
	/**
	 * to the original rate (-50 .. +100 %);<br>
	 * Original signature : <code>void soundtouch_setRateChange(HANDLE, float)</code>
	 */
	__attribute__((dllimport)) __stdcall void soundtouch_setRateChange(HANDLE h, float newRate);
	/**
	 * to the original tempo (-50 .. +100 %);<br>
	 * Original signature : <code>void soundtouch_setTempoChange(HANDLE, float)</code>
	 */
	__attribute__((dllimport)) __stdcall void soundtouch_setTempoChange(HANDLE h, float newTempo);
	/**
	 * represent lower pitches, larger values higher pitch.<br>
	 * Original signature : <code>void soundtouch_setPitch(HANDLE, float)</code>
	 */
	__attribute__((dllimport)) __stdcall void soundtouch_setPitch(HANDLE h, float newPitch);
	/**
	 * (-1.00 .. +1.00);<br>
	 * Original signature : <code>void soundtouch_setPitchOctaves(HANDLE, float)</code>
	 */
	__attribute__((dllimport)) __stdcall void soundtouch_setPitchOctaves(HANDLE h, float newPitch);
	/**
	 * (-12 .. +12);<br>
	 * Original signature : <code>void soundtouch_setPitchSemiTones(HANDLE, float)</code>
	 */
	__attribute__((dllimport)) __stdcall void soundtouch_setPitchSemiTones(HANDLE h, float newPitch);
	/**
	 * Sets the number of channels, 1 = mono, 2 = stereo<br>
	 * Original signature : <code>void soundtouch_setChannels(HANDLE, unsigned int)</code>
	 */
	__attribute__((dllimport)) __stdcall void soundtouch_setChannels(HANDLE h, unsigned int numChannels);
	/**
	 * Sets sample rate.<br>
	 * Original signature : <code>void soundtouch_setSampleRate(HANDLE, unsigned int)</code>
	 */
	__attribute__((dllimport)) __stdcall void soundtouch_setSampleRate(HANDLE h, unsigned int srate);
	/**
	 * in the middle of a sound stream.<br>
	 * Original signature : <code>void soundtouch_flush(HANDLE)</code>
	 */
	__attribute__((dllimport)) __stdcall void soundtouch_flush(HANDLE h);
	/**
	 * calling this function, otherwise throws a runtime_error exception.<br>
	 * Original signature : <code>void soundtouch_putSamples(HANDLE, const float*, unsigned int)</code><br>
	 * @param samples < Pointer to sample buffer.<br>
	 * @param numSamples < Number of samples in buffer. Notice
	 */
	__attribute__((dllimport)) __stdcall void soundtouch_putSamples(HANDLE h, const float* samples, unsigned int numSamples);
	/**
	 * buffers.<br>
	 * Original signature : <code>void soundtouch_clear(HANDLE)</code>
	 */
	__attribute__((dllimport)) __stdcall void soundtouch_clear(HANDLE h);
	/**
	 * \return 'TRUE' if the setting was succesfully changed<br>
	 * Original signature : <code>BOOL soundtouch_setSetting(HANDLE, int, int)</code><br>
	 * @param settingId < Setting ID number. see SETTING_... defines.<br>
	 * @param value < New setting value.
	 */
	__attribute__((dllimport)) __stdcall BOOL soundtouch_setSetting(HANDLE h, int settingId, int value);
	/**
	 * \return the setting value.<br>
	 * Original signature : <code>int soundtouch_getSetting(HANDLE, int)</code><br>
	 * @param settingId < Setting ID number, see SETTING_... defines.
	 */
	__attribute__((dllimport)) __stdcall int soundtouch_getSetting(HANDLE h, int settingId);
	/**
	 * Returns number of samples currently unprocessed.<br>
	 * Original signature : <code>int soundtouch_numUnprocessedSamples(HANDLE)</code>
	 */
	__attribute__((dllimport)) unsigned __stdcall int soundtouch_numUnprocessedSamples(HANDLE h);
	/**
	 * with 'ptrBegin' function.<br>
	 * Original signature : <code>int soundtouch_receiveSamples(HANDLE, float*, unsigned int)</code><br>
	 * @param outBuffer < Buffer where to copy output samples.<br>
	 * @param maxSamples < How many samples to receive at max.
	 */
	__attribute__((dllimport)) unsigned __stdcall int soundtouch_receiveSamples(HANDLE h, float* outBuffer, unsigned int maxSamples);
	/**
	 * Returns number of samples currently available.<br>
	 * Original signature : <code>int soundtouch_numSamples(HANDLE)</code>
	 */
	__attribute__((dllimport)) unsigned __stdcall int soundtouch_numSamples(HANDLE h);
	/**
	 * Returns nonzero if there aren't any samples available for outputting.<br>
	 * Original signature : <code>int soundtouch_isEmpty(HANDLE)</code>
	 */
	__attribute__((dllimport)) __stdcall int soundtouch_isEmpty(HANDLE h);
}
