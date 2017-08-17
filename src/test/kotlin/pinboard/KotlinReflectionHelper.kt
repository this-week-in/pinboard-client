package pinboard

import java.lang.reflect.Method
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.jvm.kotlinFunction

private class KotlinReflectionHelper {
    companion object {

        @Suppress("UNCHECKED_CAST")
        @JvmStatic fun <T> callKotlinMethodWithNamedParms(instance: Any, method: Method, parmMap: Map<String, Any>): T {
            val callable: KFunction<T> = method.kotlinFunction as? KFunction<T> ?: throw IllegalStateException("Method is not a Kotlin method")
            val unusedParms = HashSet(parmMap.keys)
            val callableParms = hashMapOf<KParameter, Any?>()
            callable.parameters.map { parm ->
                if (parm.kind == KParameter.Kind.INSTANCE) {
                    callableParms.put(parm, instance)
                } else if (parm.kind == KParameter.Kind.VALUE && parmMap.contains(parm.name)) {
                    unusedParms.remove(parm.name)
                    callableParms.put(parm, parmMap.get(parm.name))
                } else if (parm.kind == KParameter.Kind.VALUE) {
                    if (parm.isOptional) {
                        // default value will be used!
                    } else {
                        throw IllegalStateException("Missing required parameter ${parm.name}")
                    }
                } else {
                    throw IllegalStateException("Cannot call methods that are not direct instance methods")
                }
            }
            if (unusedParms.isNotEmpty()) {
                throw IllegalStateException("Unrecognized parameters passed to function: $unusedParms")
            }
            return method.kotlinFunction?.callBy(callableParms) as T
        }
    }
}