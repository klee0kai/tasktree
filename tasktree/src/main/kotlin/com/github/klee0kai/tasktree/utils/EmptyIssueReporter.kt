package com.github.klee0kai.tasktree.utils

import com.android.builder.errors.EvalIssueException
import com.android.builder.errors.IssueReporter

object EmptyIssueReporter : IssueReporter() {
    override fun hasIssue(type: Type): Boolean = false
    override fun reportIssue(type: Type, severity: Severity, exception: EvalIssueException) = Unit
}