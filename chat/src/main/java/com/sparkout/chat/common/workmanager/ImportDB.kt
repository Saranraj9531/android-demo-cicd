package com.sparkout.chat.common.workmanager

import org.jetbrains.annotations.NotNull
import java.lang.StringBuilder

/**
 *Created by Nivetha S on 08-03-2021.
 */
class ImportDB(var col: String,
               var values: @NotNull StringBuilder,
               var tableName: @NotNull String) {
}