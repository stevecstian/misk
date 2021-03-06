package misk.web.extractors

import misk.web.PathParam
import misk.web.PathPattern
import misk.web.Request
import misk.web.actions.WebAction
import java.util.regex.Matcher
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.findAnnotation

/**
 * Creates a [ParameterExtractor] that extracts the URL parameter with the same name as [parameter]
 * and returns it as a [String]. If the parameter name doesn't occur in [pathPattern], returns null.
 */
object PathPatternParameterExtractorFactory : ParameterExtractor.Factory {
    override fun create(
            function: KFunction<*>,
            parameter: KParameter,
            pathPattern: PathPattern
    ): ParameterExtractor? {
        val pathParamAnnotation = parameter.findAnnotation<PathParam>() ?: return null
        val parameterName =
                if (pathParamAnnotation.value.isBlank()) parameter.name
                else pathParamAnnotation.value

        val patternIndex = pathPattern.variableNames.indexOf(parameterName)
        if (patternIndex == -1) return null

        return object : ParameterExtractor {
            override fun extract(webAction: WebAction, request: Request,
                    pathMatcher: Matcher): Any? {
                return pathMatcher.group(patternIndex + 1)
            }
        }
    }
}
