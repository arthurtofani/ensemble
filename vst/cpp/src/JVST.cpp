/*******************************************************************************
* This program and the accompanying materials
* are made available under the terms of the Common Public License v1.0
* which accompanies this distribution, and is available at 
* http://www.eclipse.org/legal/cpl-v10.html
* 
* Contributors:
*     Peter Smith
*******************************************************************************/

#include "JVST.h"
#include <jni.h>

static JavaVM* jvm;
static jclass VST_CLASS;
static jmethodID CALLBACK_METHOD;

long VSTCALLBACK audioHost(AEffect *effect, long opcode, long index, long value, void *ptr, float opt);

JNIEXPORT jint JNICALL
JNI_OnLoad(JavaVM *vm, void *reserved)
{
	jvm = vm;
	JNIEnv* env;
	vm->GetEnv((void**) &env, JNI_VERSION_1_4);
	VST_CLASS = env->FindClass("org/boris/jvst/VST");
	if(VST_CLASS == 0) return 0;
	CALLBACK_METHOD = env->GetStaticMethodID(VST_CLASS, "callback", "(JJJJJF)J");
	if(CALLBACK_METHOD == 0) return 0;
	return JNI_VERSION_1_4;
}

JNIEXPORT jlong JNICALL 
Java_org_boris_jvst_JNI_loadLibrary(JNIEnv *env, jclass obj, jstring filename)
{
	jboolean iscopy = false;
	const char* fstr = env->GetStringUTFChars(filename, &iscopy);
	if(fstr == NULL)
		return 0;

	HINSTANCE hDLL = LoadLibrary(fstr);
	env->ReleaseStringUTFChars(filename, fstr);
	return (jlong) hDLL;
}

JNIEXPORT jlong JNICALL 
Java_org_boris_jvst_JNI_loadEffect(JNIEnv *env, jclass obj, jlong library)
{
	if(library == 0)
		return 0;

	AEffect* (__cdecl* effectMain)(audioMasterCallback);
	effectMain=(AEffect*(__cdecl*)(audioMasterCallback))GetProcAddress((HINSTANCE) library, "main");
	if(effectMain == NULL) 
		return 0;

	return (jlong) effectMain((audioMasterCallback) audioHost);
}

JNIEXPORT void JNICALL 
Java_org_boris_jvst_JNI_fillEffect(JNIEnv *env, jclass obj, jlong library, jlong ptr, jobject effect)
{
	AEffect* p = (AEffect*) ptr;
	jclass c = env->FindClass("org/boris/jvst/AEffect");
	env->SetIntField(effect, env->GetFieldID(c, "magic", "I"), p->magic);
	env->SetIntField(effect, env->GetFieldID(c, "numPrograms", "I"), p->numPrograms);
	env->SetIntField(effect, env->GetFieldID(c, "numParams", "I"), p->numParams);
	env->SetIntField(effect, env->GetFieldID(c, "numInputs",  "I"),p->numInputs);
	env->SetIntField(effect, env->GetFieldID(c, "numOutputs", "I"), p->numOutputs);
	env->SetIntField(effect, env->GetFieldID(c, "flags", "I"), p->flags);
	env->SetIntField(effect, env->GetFieldID(c, "initialDelay",  "I"),p->initialDelay);
	env->SetIntField(effect, env->GetFieldID(c, "uniqueID", "I"), p->uniqueID);
	env->SetIntField(effect, env->GetFieldID(c, "version",  "I"),p->version);
	env->SetLongField(effect, env->GetFieldID(c, "library", "J"), library);
	env->SetLongField(effect, env->GetFieldID(c, "ptr", "J"), ptr);
}

JNIEXPORT jint JNICALL 
Java_org_boris_jvst_JNI_dispatcher(JNIEnv *env, jclass obj, jlong ptr, 
	jint opcode, jint index, jlong valuePtr, jlong dPtr, jfloat opt)
{
	AEffect* p = (AEffect*) ptr;
	return p->dispatcher(p, (int) opcode, (int) index, (int) valuePtr, (void *) dPtr, (float) opt);
}

JNIEXPORT jint JNICALL 
Java_org_boris_jvst_JNI_canDo(JNIEnv *env, jclass obj, jlong ptr, jstring doStr)
{
	AEffect* p = (AEffect*) ptr;
	jboolean iscopy = false;
	const char* str = env->GetStringUTFChars(doStr, &iscopy);
	int res = p->dispatcher(p, effCanDo, 0,0,(void*)str,0.0f);
	env->ReleaseStringUTFChars(doStr, str);
	return res;
}

JNIEXPORT jobject JNICALL 
Java_org_boris_jvst_JNI_getPinProperties(JNIEnv *env, jclass obj, jlong ptr, jint index, jboolean input)
{
	AEffect* p = (AEffect*) ptr;
	int code = input ? effGetInputProperties : effGetOutputProperties;
	VstPinProperties temp;
	if(p->dispatcher(p,code,index,0,&temp,0.0f)) {
		jclass c = env->FindClass("org/boris/jvst/struct/VstPinProperties");
		if(c == NULL) {
			env->Throw(env->ExceptionOccurred());
			return NULL;
		}
		jobject o = env->AllocObject(c);
		env->SetObjectField(o, env->GetFieldID(c, "label", "Ljava/lang/String;"), env->NewStringUTF(temp.label));
		env->SetObjectField(o, env->GetFieldID(c, "shortLabel", "Ljava/lang/String;"), env->NewStringUTF(temp.shortLabel));
		env->SetIntField(o, env->GetFieldID(c, "flags", "I"), temp.flags);
		env->SetIntField(o, env->GetFieldID(c, "arrangementType", "I"), temp.arrangementType);
		jbyteArray ba = env->NewByteArray(48);
		env->SetByteArrayRegion(ba, 0, 48, (const jbyte*) temp.future);
		env->SetObjectField(o, env->GetFieldID(c, "future", "[B"), ba);
		return o;
	}
	return NULL;
}

JNIEXPORT jobject JNICALL 
Java_org_boris_jvst_JNI_getParameterProperties(JNIEnv *env, jclass obj, jlong ptr, jint index)
{
	AEffect* p = (AEffect*) ptr;
	VstParameterProperties temp;
	if(p->dispatcher(p,effGetParameterProperties,index,0,&temp,0.0f)) {
		jclass c = env->FindClass("org/boris/jvst/struct/VstParameterProperties");
		if(c == NULL) {
			env->Throw(env->ExceptionOccurred());
			return NULL;
		}
		jobject o = env->AllocObject(c);
		env->SetFloatField(o, env->GetFieldID(c, "stepFloat", "F"), temp.stepFloat);
		env->SetFloatField(o, env->GetFieldID(c, "smallStepFloat", "F"), temp.smallStepFloat);
		env->SetFloatField(o, env->GetFieldID(c, "largeStepFloat", "F"), temp.largeStepFloat);
		env->SetObjectField(o, env->GetFieldID(c, "label", "Ljava/lang/String;"), env->NewStringUTF(temp.label));
		env->SetIntField(o, env->GetFieldID(c, "flags", "I"), temp.flags);
		env->SetIntField(o, env->GetFieldID(c, "minInteger", "I"), temp.minInteger);
		env->SetIntField(o, env->GetFieldID(c, "maxInteger", "I"), temp.maxInteger);
		env->SetIntField(o, env->GetFieldID(c, "stepInteger", "I"), temp.stepInteger);
		env->SetIntField(o, env->GetFieldID(c, "largeStepInteger", "I"), temp.largeStepInteger);
		env->SetObjectField(o, env->GetFieldID(c, "shortLabel", "Ljava/lang/String;"), env->NewStringUTF(temp.shortLabel));
		env->SetIntField(o, env->GetFieldID(c, "displayIndex", "I"), temp.displayIndex);
		env->SetIntField(o, env->GetFieldID(c, "category", "I"), temp.category);
		env->SetIntField(o, env->GetFieldID(c, "numParametersInCategory", "I"), temp.numParametersInCategory);
		env->SetIntField(o, env->GetFieldID(c, "reserved", "I"), temp.reserved);
		env->SetObjectField(o, env->GetFieldID(c, "categoryLabel", "Ljava/lang/String;"), env->NewStringUTF(temp.categoryLabel));
		jbyteArray ba = env->NewByteArray(16);
		env->SetByteArrayRegion(ba, 0, 16, (const jbyte*) temp.future);
		env->SetObjectField(o, env->GetFieldID(c, "future", "[B"), ba);

		return o;
	}
	return NULL;
}

JNIEXPORT jobject JNICALL 
Java_org_boris_jvst_JNI_editGetRect(JNIEnv *env, jclass obj, jlong ptr)
{
	AEffect* p = (AEffect*) ptr;
	ERect* eRect = 0;
	p->dispatcher(p, effEditGetRect, 0, 0, &eRect, 0);
	if(eRect) {
		jclass c = env->FindClass("org/boris/jvst/struct/ERect");
		if(c == NULL) {
			env->Throw(env->ExceptionOccurred());
			return NULL;
		}
		jobject o = env->AllocObject(c);
		env->SetIntField(o, env->GetFieldID(c, "top", "I"), eRect->top);
		env->SetIntField(o, env->GetFieldID(c, "left", "I"), eRect->left);
		env->SetIntField(o, env->GetFieldID(c, "bottom", "I"), eRect->bottom);
		env->SetIntField(o, env->GetFieldID(c, "right", "I"), eRect->right);
		return o;
	}

	return NULL;
}

JNIEXPORT jstring JNICALL 
Java_org_boris_jvst_JNI_dispatcherS(JNIEnv *env, jclass obj, jlong ptr, jint opcode, jint index)
{
	AEffect* p = (AEffect*) ptr;
	char strProgramName[512];
	p->dispatcher(p,opcode,index,0,strProgramName,0.0f);
	return env->NewStringUTF(strProgramName);
}

JNIEXPORT void JNICALL 
Java_org_boris_jvst_JNI_setParameter(JNIEnv *env, jclass obj, jlong ptr, jint index, jfloat value)
{
	AEffect* p = (AEffect*) ptr;
	p->setParameter(p, index, value);
}

JNIEXPORT jfloat JNICALL 
Java_org_boris_jvst_JNI_getParameter(JNIEnv *env, jclass obj, jlong ptr, jint index)
{
	AEffect* p = (AEffect*) ptr;
	return p->getParameter(p, index);
}

JNIEXPORT void JNICALL 
Java_org_boris_jvst_JNI_processReplacing(JNIEnv *env, jclass obj, jlong ptr, jobjectArray inputs, jobjectArray outputs, jint blocksize)
{
	AEffect* p = (AEffect*) ptr;
	int ilen = env->GetArrayLength(inputs);
	int olen = env->GetArrayLength(outputs);
	float** pInputs = new float*[ilen];
	float** pOutputs = new float*[olen];
	jboolean iscopy = false;
	for(int i = 0; i < ilen; i++) {
		jarray ia = (jarray) env->GetObjectArrayElement(inputs, i);
		if(ia)
			pInputs[i] = (float*) env->GetPrimitiveArrayCritical(ia, &iscopy);
	}
	for(int i = 0; i < olen; i++) {
		jarray oa = (jarray) env->GetObjectArrayElement(outputs, i);
		if(oa)
			pOutputs[i] = (float*) env->GetPrimitiveArrayCritical(oa, &iscopy);
	}
	p->processReplacing(p, pInputs, pOutputs, blocksize);
	for(int i = 0; i < ilen; i++) {
		jarray ia = (jarray) env->GetObjectArrayElement(inputs, i);
		if(ia)
			env->ReleasePrimitiveArrayCritical(ia, pInputs[i], 0);
	}
	for(int i = 0; i < olen; i++) {
		jarray oa = (jarray) env->GetObjectArrayElement(outputs, i);
		if(oa)
			env->ReleasePrimitiveArrayCritical(oa, pOutputs[i], 0);
	}
	delete [] pInputs;
	delete [] pOutputs;
}

JNIEXPORT void JNICALL 
Java_org_boris_jvst_JNI_freeLibrary(JNIEnv *env, jclass obj, jlong library)
{
	FreeLibrary((HINSTANCE) library);
}

long VSTCALLBACK audioHost(AEffect *effect, long opcode, long index, long value, void *ptr, float opt)
{
	JNIEnv* env;
	jvm->AttachCurrentThread((void **)&env, 0);
	return (long) env->CallStaticLongMethod(VST_CLASS, CALLBACK_METHOD, (jlong) effect, (jlong) opcode, (jlong) index, (jlong) value, (jlong) ptr, (jfloat) opt);
}