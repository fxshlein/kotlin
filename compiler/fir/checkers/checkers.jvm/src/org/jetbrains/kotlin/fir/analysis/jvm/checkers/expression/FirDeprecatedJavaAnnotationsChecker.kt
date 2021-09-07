/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.analysis.jvm.checkers.expression

import org.jetbrains.kotlin.builtins.StandardNames
import org.jetbrains.kotlin.fir.FirRealSourceElementKind
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.checkers.expression.FirAnnotationChecker
import org.jetbrains.kotlin.fir.analysis.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.fir.analysis.diagnostics.jvm.FirJvmErrors
import org.jetbrains.kotlin.fir.analysis.diagnostics.reportOn
import org.jetbrains.kotlin.fir.expressions.FirAnnotation
import org.jetbrains.kotlin.fir.types.ConeClassLikeType
import org.jetbrains.kotlin.fir.types.coneTypeSafe
import org.jetbrains.kotlin.load.java.JvmAnnotationNames
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName

object FirDeprecatedJavaAnnotationsChecker : FirAnnotationChecker() {

    private val javaToKotlinNameMap: Map<ClassId, FqName> =
        mapOf(
            ClassId.topLevel(JvmAnnotationNames.TARGET_ANNOTATION) to StandardNames.FqNames.target,
            ClassId.topLevel(JvmAnnotationNames.RETENTION_ANNOTATION) to StandardNames.FqNames.retention,
            ClassId.topLevel(JvmAnnotationNames.DEPRECATED_ANNOTATION) to StandardNames.FqNames.deprecated,
            ClassId.topLevel(JvmAnnotationNames.DOCUMENTED_ANNOTATION) to StandardNames.FqNames.mustBeDocumented
        )

    override fun check(expression: FirAnnotation, context: CheckerContext, reporter: DiagnosticReporter) {
        if (context.containingDeclarations.lastOrNull()?.source?.kind != FirRealSourceElementKind) return

        val lookupTag = expression.annotationTypeRef.coneTypeSafe<ConeClassLikeType>()?.lookupTag ?: return
        javaToKotlinNameMap[lookupTag.classId]?.let { betterName ->
            reporter.reportOn(expression.source, FirJvmErrors.DEPRECATED_JAVA_ANNOTATION, betterName, context)
        }
    }
}
