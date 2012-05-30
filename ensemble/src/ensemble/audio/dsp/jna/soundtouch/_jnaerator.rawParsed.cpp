extern ""C"" {
typedef void *HANDLE;
	/// Create a new instance of SoundTouch processor.
	__attribute__((dllimport)) __stdcall HANDLE soundtouch_createInstance();
	/// Destroys a SoundTouch processor instance.
	__attribute__((dllimport)) __stdcall void soundtouch_destroyInstance(HANDLE h);
	/// Get SoundTouch library version string
	__attribute__((dllimport)) const __stdcall char* soundtouch_getVersionString();
	/// environments that can't properly handle character string as return value
	__attribute__((dllimport)) __stdcall void soundtouch_getVersionString2(char* versionString, int bufferSize);
	/// Get SoundTouch library version Id
	__attribute__((dllimport)) unsigned __stdcall int soundtouch_getVersionId();
	/// represent slower rate, larger faster rates.
	__attribute__((dllimport)) __stdcall void soundtouch_setRate(HANDLE h, float newRate);
	/// represent slower tempo, larger faster tempo.
	__attribute__((dllimport)) __stdcall void soundtouch_setTempo(HANDLE h, float newTempo);
	/// to the original rate (-50 .. +100 %);
	__attribute__((dllimport)) __stdcall void soundtouch_setRateChange(HANDLE h, float newRate);
	/// to the original tempo (-50 .. +100 %);
	__attribute__((dllimport)) __stdcall void soundtouch_setTempoChange(HANDLE h, float newTempo);
	/// represent lower pitches, larger values higher pitch.
	__attribute__((dllimport)) __stdcall void soundtouch_setPitch(HANDLE h, float newPitch);
	/// (-1.00 .. +1.00);
	__attribute__((dllimport)) __stdcall void soundtouch_setPitchOctaves(HANDLE h, float newPitch);
	/// (-12 .. +12);
	__attribute__((dllimport)) __stdcall void soundtouch_setPitchSemiTones(HANDLE h, float newPitch);
	/// Sets the number of channels, 1 = mono, 2 = stereo
	__attribute__((dllimport)) __stdcall void soundtouch_setChannels(HANDLE h, unsigned int numChannels);
	/// Sets sample rate.
	__attribute__((dllimport)) __stdcall void soundtouch_setSampleRate(HANDLE h, unsigned int srate);
	/// in the middle of a sound stream.
	__attribute__((dllimport)) __stdcall void soundtouch_flush(HANDLE h);
	/// calling this function, otherwise throws a runtime_error exception.
	__attribute__((dllimport)) __stdcall void soundtouch_putSamples(HANDLE h, const float* samples, unsigned int numSamples);
	/// buffers.
	__attribute__((dllimport)) __stdcall void soundtouch_clear(HANDLE h);
	/// \return 'TRUE' if the setting was succesfully changed
	__attribute__((dllimport)) __stdcall BOOL soundtouch_setSetting(HANDLE h, int settingId, int value);
	/// \return the setting value.
	__attribute__((dllimport)) __stdcall int soundtouch_getSetting(HANDLE h, int settingId);
	/// Returns number of samples currently unprocessed.
	__attribute__((dllimport)) unsigned __stdcall int soundtouch_numUnprocessedSamples(HANDLE h);
	/// with 'ptrBegin' function.
	__attribute__((dllimport)) unsigned __stdcall int soundtouch_receiveSamples(HANDLE h, float* outBuffer, unsigned int maxSamples);
	/// Returns number of samples currently available.
	__attribute__((dllimport)) unsigned __stdcall int soundtouch_numSamples(HANDLE h);
	/// Returns nonzero if there aren't any samples available for outputting.
	__attribute__((dllimport)) __stdcall int soundtouch_isEmpty(HANDLE h);
}
