package com.alternadv.vedhelper.utils

import android.content.Context
import com.alternadv.vedhelper.model.CalcResultModel
import com.alternadv.vedhelper.model.CarCalcResultModel
import com.alternadv.vedhelper.model.CarReportRows
import com.alternadv.vedhelper.model.ReportRowModel
import com.alternadv.vedhelper.model.VehicleTypes
import com.alternadv.vedhelper.ui.screen.carcalc.CarCalcState
import com.itextpdf.io.font.FontProgramFactory
import com.itextpdf.io.font.PdfEncodings
import com.itextpdf.io.image.ImageDataFactory
import com.itextpdf.kernel.font.PdfFont
import com.itextpdf.kernel.font.PdfFontFactory
import com.itextpdf.kernel.geom.PageSize
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.*
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.properties.HorizontalAlignment
import com.itextpdf.layout.properties.TextAlignment
import com.itextpdf.layout.properties.UnitValue
import com.itextpdf.layout.properties.VerticalAlignment
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


object PdfGenerator {

    fun generateCalcResultPdf(
        context: Context,
        outputPath: String? = null,
        title: String,
        parameters: List<ReportRowModel>,
        resultTitle: String = "",
        results: List<ReportRowModel> = emptyList(),
        result2Title: String = "",
        results2: List<ReportRowModel> = emptyList(),
        comments: String? = null
    ): File {
        val outFile = outputPath?.let {
            File(context.cacheDir, it)
        } ?: File(context.cacheDir, "calc_result.pdf")

        val pdfWriter = PdfWriter(outFile)
        val pdfDocument = PdfDocument(pdfWriter)
        val document = Document(pdfDocument, PageSize.A4)
        document.setMargins(20f, 20f, 20f, 20f)

        // --- Подключаем шрифт (кириллица) ---
        val fontBytes = context.assets.open("fonts/roboto.ttf").use { it.readBytes() }
        val fontProgram = FontProgramFactory.createFont(fontBytes, false)
        val font: PdfFont = PdfFontFactory.createFont(fontProgram, PdfEncodings.IDENTITY_H)

        val fontBoldPath = context.assets.open("fonts/roboto_bold.ttf").use { it.readBytes() }
        val fontBoldProgram = FontProgramFactory.createFont(fontBoldPath, false)
        val fontBold: PdfFont = PdfFontFactory.createFont(fontBoldProgram, PdfEncodings.IDENTITY_H)

        /*
        val fontPath = "F:\\Kotlin\\Test\\templates\\roboto.ttf"   // относительный путь от рабочей директории
        val fontProgram = FontProgramFactory.createFont(fontPath)
        val font = PdfFontFactory.createFont(fontProgram, PdfEncodings.IDENTITY_H)

        val fontBoldPath = "F:\\Kotlin\\Test\\templates\\roboto_bold.ttf"
        val fontBoldProgram = FontProgramFactory.createFont(fontBoldPath)
        val fontBold = PdfFontFactory.createFont(fontBoldProgram, PdfEncodings.IDENTITY_H)
        */

        // --- Шапка: логотип + текст ---
        val logoStream = context.assets.open("alterna-logo.jpg")
        val logoBytes = logoStream.readBytes()
        logoStream.close()
        val logo = Image(ImageDataFactory.create(logoBytes)).scaleToFit(196f, 150f)

        /*
        val logoPath = "F:\\Kotlin\\Test\\templates\\alterna-logo.jpg"
        val logo = Image(ImageDataFactory.create(logoPath)).scaleToFit(131f, 100f)
         */

        logo.setHorizontalAlignment(HorizontalAlignment.CENTER)

        val headerTable = Table(UnitValue.createPercentArray(floatArrayOf(1f, 2f))).useAllAvailableWidth()
        headerTable.addCell(Cell().setTextAlignment(TextAlignment.CENTER).add(logo).setBorder(null))
        val headerText = """
        Транспортно-логистическая компания «Альтерна» - таможенный представитель с многолетним успешным опытом работы в сфере ВЭД.
        Мы занимаемся таможенным оформлением и доставкой грузов любой категории из всех стран Азии (Китай, Корея, Япония и т.д.) в Россию.
        
        Телефон: +7 (902) 050-40-50
        Email: broker@alterna.ltd
        Сайт: www.alternadv.com        
    """.trimIndent()
        val headerParagraph = Paragraph(headerText)
            .setFont(font)
            .setFontSize(10f)
            .setTextAlignment(TextAlignment.LEFT)
            .setFixedLeading(12f)
        headerTable.addCell(Cell().add(headerParagraph).setBorder(null))
        document.add(headerTable)

        document.add(Paragraph(title).setFont(fontBold).setFontSize(12f).setTextAlignment(TextAlignment.CENTER).setPaddingBottom(0f))

        // --- Таблица результатов 1 ---
        if (resultTitle.isNotBlank()) document.add(Paragraph(resultTitle).setFont(font).setFontSize(10f))
        if (results.isNotEmpty()) {
            document.add(buildTable(results, font, fontBold, false))
        }

        // --- Таблица результатов 2 ---
        if (result2Title.isNotBlank()) document.add(Paragraph(result2Title).setFont(font).setFontSize(10f))
        if (results2.isNotEmpty()) {
            document.add(buildTable(results2, font, fontBold, false))
        }

        // --- Параметры ---
        document.add(Paragraph("Параметры").setFont(font).setFontSize(10f))
        document.add(buildTable(parameters, font, fontBold, true))

        // --- Дата и комментарий ---
        document.add(Paragraph("Дата расчета: ${getCurrentDateTimeRu()}").setFont(font).setFontSize(10f))
        comments?.takeIf { it.isNotBlank() }?.let {
            document.add(Paragraph(it).setFont(font).setFontSize(10f).setFixedLeading(12f).setPaddingBottom(0f))
        }

        // --- Футер (только на одной странице) ---
        val footer = Paragraph(
            "Данный расчет не является публичной офертой.\n" +
                    "За точным расчетом и дополнительной информацией обращайтесь в нашу компанию."
        )
            .setFont(font).setFontSize(8f)
            .setTextAlignment(TextAlignment.CENTER)
            .setHorizontalAlignment(HorizontalAlignment.CENTER)
        document.showTextAligned(
            footer,
            297f,
            35f,
            pdfDocument.numberOfPages,
            TextAlignment.CENTER,
            VerticalAlignment.TOP,
            0f
        )

        document.close()
        return outFile
    }

    // --- Строим таблицу ---
    private fun buildTable(
        data: List<ReportRowModel>,
        fontNormal: PdfFont,
        fontBold: PdfFont,
        use2Cols: Boolean
    ): Table {
        val table = if (use2Cols) Table(UnitValue.createPercentArray(floatArrayOf(1f, 2f)))
        else Table(UnitValue.createPercentArray(floatArrayOf(2f, 3f, 1f, 1f)))
        table.useAllAvailableWidth()

        table.setPadding(1f)

        if (!use2Cols) {
            table.addHeaderCell(
                Cell()
                    .add(Paragraph("Вид")
                        .setFontSize(10f)
                        .setMarginTop(0f)
                        .setMarginBottom(0f)
                        .setPadding(0f)
                        .setFont(fontBold))
                    .setTextAlignment(TextAlignment.CENTER)
                    .setPadding(1f)
                    .setMargin(0f)
            )
            table.addHeaderCell(
                Cell()
                    .add(Paragraph("Ставка")
                        .setFontSize(10f)
                        .setMarginTop(0f)
                        .setMarginBottom(0f)
                        .setPadding(0f)
                        .setFont(fontBold))
                    .setTextAlignment(TextAlignment.CENTER)
                    .setPadding(1f)
                    .setMargin(0f)
            )
            table.addHeaderCell(
                Cell()
                    .add(Paragraph("Сумма, руб")
                        .setFontSize(10f)
                        .setMarginTop(0f)
                        .setMarginBottom(0f)
                        .setPadding(0f)
                        .setFont(fontBold))
                    .setTextAlignment(TextAlignment.CENTER)
                    .setPadding(1f)
                    .setMargin(0f)
            )
            table.addHeaderCell(
                Cell()
                    .add(Paragraph("Сумма, $")
                        .setFontSize(10f)
                        .setMarginTop(0f)
                        .setMarginBottom(0f)
                        .setPadding(0f)
                        .setFont(fontBold))
                    .setTextAlignment(TextAlignment.CENTER)
                    .setPadding(1f)
                    .setMargin(0f)
            )
        }

        data.forEach {
            val fontStyle = if (it.bold) fontBold else fontNormal
            table.addCell(Cell().setPadding(1f).add(Paragraph(it.first).setFont(fontStyle).setFontSize(10f)))
            table.addCell(Cell().setPadding(1f).add(Paragraph(it.second).setFont(fontStyle).setFontSize(10f)))
            if (!use2Cols) {
                table.addCell(
                    Cell()
                        .setPadding(1f)
                        .add(
                            Paragraph(it.third).setFont(fontStyle).setFontSize(10f).setTextAlignment(
                                TextAlignment.RIGHT
                            )
                        )
                )
                table.addCell(
                    Cell()
                        .setPadding(1f)
                        .add(
                            Paragraph(it.fourth).setFont(fontStyle).setFontSize(10f).setTextAlignment(TextAlignment.RIGHT)
                        )
                )
            }
        }
        return table
    }

    /*
    fun generateCalcResultPdf(
        context: Context,
        outputPath: String? = null,
        title: String,
        parameters: List<ReportRowModel>,
        resultTitle: String = "",
        results: List<ReportRowModel> = emptyList(),
        result2Title: String = "",
        results2: List<ReportRowModel> = emptyList(),
        comments: String? = null
    ): File {
        val outFile = outputPath?.let {
            File(context.cacheDir, it)
        } ?: File(context.cacheDir, "calc_result.pdf")

        val pdfWriter = PdfWriter(outFile)
        val pdfDocument = PdfDocument(pdfWriter)
        val document = Document(pdfDocument, PageSize.A4)
        document.setMargins(20f, 20f, 40f, 20f)

        // --- Подключаем шрифт (кириллица) ---
        val fontBytes = context.assets.open("fonts/roboto.ttf").use { it.readBytes() }
        val fontProgram = FontProgramFactory.createFont(fontBytes, false)
        val font: PdfFont = PdfFontFactory.createFont(fontProgram, PdfEncodings.IDENTITY_H)

        // --- Шапка: логотип + текст ---
        val logoStream = context.assets.open("alterna-logo.jpg")
        val logoBytes = logoStream.readBytes()
        logoStream.close()
        val logo = Image(ImageDataFactory.create(logoBytes)).scaleToFit(100f, 50f)

        val headerTable = Table(UnitValue.createPercentArray(floatArrayOf(2f, 3f))).useAllAvailableWidth()
        headerTable.addCell(Cell().add(logo).setBorder(null))
        val headerText = """
        Транспортно-логистическая компания «Альтерна» - таможенный представитель с многолетним успешным опытом работы в сфере ВЭД.
        Мы занимаемся таможенным оформлением и доставкой грузов любой категории из всех стран Азии (Китай, Корея, Япония и т.д.) в Россию.
    """.trimIndent()
        val headerParagraph = Paragraph(headerText).setFont(font).setFontSize(10f).setTextAlignment(TextAlignment.LEFT)
        headerTable.addCell(Cell().add(headerParagraph).setBorder(null))
        document.add(headerTable)

        // --- Контакты ---
        val contacts = Table(UnitValue.createPercentArray(floatArrayOf(1f, 2f))).useAllAvailableWidth()
        fun addContact(label: String, value: String) {
            contacts.addCell(Cell().add(Paragraph(label).setFont(font).setFontSize(10f)).setBorder(null))
            contacts.addCell(Cell().add(Paragraph(value).setFont(font).setFontSize(10f)).setBorder(null))
        }
        addContact("Телефон:", "+7 (902) 050-40-50")
        addContact("Email:", "broker@alterna.ltd")
        addContact("Сайт:", "www.alternadv.com")
        document.add(contacts)

        document.add(Paragraph(title).setFont(font).setFontSize(14f).setTextAlignment(TextAlignment.CENTER))

        // --- Таблица результатов 1 ---
        if (resultTitle.isNotBlank()) document.add(Paragraph(resultTitle).setFont(font))
        if (results.isNotEmpty()) {
            document.add(buildTable(results, font, false))
        }

        // --- Таблица результатов 2 ---
        if (result2Title.isNotBlank()) document.add(Paragraph(result2Title).setFont(font))
        if (results2.isNotEmpty()) {
            document.add(buildTable(results2, font, false))
        }

        // --- Параметры ---
        document.add(Paragraph("Параметры").setFont(font))
        document.add(buildTable(parameters, font, true))

        // --- Дата и комментарий ---
        document.add(Paragraph("Дата расчета: ${getCurrentDateTimeRu()}").setFont(font))
        comments?.takeIf { it.isNotBlank() }?.let {
            document.add(Paragraph(it).setFont(font))
        }

        // --- Футер (только на одной странице) ---
        val footer = Paragraph(
            "Данный расчет не является публичной офертой.\n" +
                    "За точным расчетом и дополнительной информацией обращайтесь в нашу компанию."
        )
            .setFont(font).setFontSize(8f)
            .setTextAlignment(TextAlignment.CENTER)
            .setHorizontalAlignment(HorizontalAlignment.CENTER)
        document.showTextAligned(footer, 297f, 20f, pdfDocument.numberOfPages, TextAlignment.CENTER, VerticalAlignment.TOP, 0f)

        document.close()
        return outFile
    }

    // --- Строим таблицу ---
    private fun buildTable(data: List<ReportRowModel>, font: PdfFont, use2Cols: Boolean): Table {
        val table = if (use2Cols) Table(UnitValue.createPercentArray(floatArrayOf(1f, 2f)))
        else Table(UnitValue.createPercentArray(floatArrayOf(1f, 2f, 1f, 1f)))
        table.useAllAvailableWidth()

        if (!use2Cols) {
            table.addHeaderCell(Cell().add(Paragraph("Вид").setFont(font)))
            table.addHeaderCell(Cell().add(Paragraph("Ставка").setFont(font)))
            table.addHeaderCell(Cell().add(Paragraph("Сумма, руб").setFont(font)))
            table.addHeaderCell(Cell().add(Paragraph("Сумма, $").setFont(font)))
        }

        data.forEach {
            val fontStyle = if (it.bold) Paragraph(it.first) else Paragraph(it.first)
            table.addCell(Cell().add(fontStyle.setFont(font)))
            table.addCell(Cell().add(Paragraph(it.second).setFont(font)))
            if (!use2Cols) {
                table.addCell(Cell().add(Paragraph(it.third).setFont(font).setTextAlignment(
                    TextAlignment.RIGHT)))
                table.addCell(Cell().add(Paragraph(it.fourth).setFont(font).setTextAlignment(TextAlignment.RIGHT)))
            }
        }
        return table
    }
*/
/*
    fun generateCalcResultPdf(
        context: Context,
        outputPath: String? = null,
        title: String,
        parameters: List<ReportRowModel>,
        resultTitle: String = "",
        results: List<ReportRowModel> = emptyList(),
        result2Title: String = "",
        results2: List<ReportRowModel> = emptyList(),
        comments: String? = null
    ): File {
        val outFile = outputPath?.let {
            File(context.cacheDir, it)
        } ?: File(context.cacheDir, "calc_result.pdf")

        val document = Document(PageSize.A4, 18f, 18f, 18f, 18f)
        val writer = PdfWriter.getInstance(document, FileOutputStream(outFile))
        writer.pageEvent = FooterEvent(
            listOf(
                "Данный расчет не является публичной офертой.",
                "За точным расчетом и дополнительной информацией обращайтесь в нашу компанию."
            )
        )

        document.open()

        // ---- Шрифт Roboto ----
        val baseFont =
            BaseFont.createFont("F:/Kotlin/Test/templates/roboto.ttf", BaseFont.IDENTITY_H, true)
        val fontNormal = Font(baseFont, 10f, Font.NORMAL)
        val fontBold = Font(baseFont, 10f, Font.BOLD)

        // ---- Шапка ----
        val headerTable = PdfPTable(2)
        headerTable.widthPercentage = 100f
        headerTable.setWidths(floatArrayOf(2f, 3f))

        // Логотип
        val inputStream = context.assets.open("alterna-logo.jpg")
        val bytes = inputStream.readBytes()
        val logo = Image.getInstance(bytes)
        logo.scaleToFit(196f, 150f)
        val logoCell = PdfPCell(logo)
        logoCell.border = Rectangle.NO_BORDER
        headerTable.addCell(logoCell)

        // Правая часть (текст + контакты)
        val rightCell = PdfPCell()
        rightCell.border = Rectangle.NO_BORDER
        rightCell.horizontalAlignment = Element.ALIGN_LEFT

        // Описание компании
        val companyParagraph = Paragraph(
            """
    Транспортно-логистическая компания «Альтерна» - таможенный представитель с многолетним успешным опытом работы в сфере ВЭД.
    Мы занимаемся таможенным оформлением и доставкой грузов любой категории из всех стран Азии (Китай, Корея, Япония и т.д.) в Россию.
    """.trimIndent(), fontNormal
        )
        companyParagraph.spacingAfter = 10f
        companyParagraph.alignment = Element.ALIGN_LEFT
        rightCell.addElement(companyParagraph)

        // ---- Таблица контактов ----
        val contactsTable = PdfPTable(2)
        contactsTable.widthPercentage = 100f
        contactsTable.setWidths(floatArrayOf(1f, 2f))

        fun addContactRow(label: String, value: String) {
            val labelCell = PdfPCell(Phrase(label, fontNormal))
            labelCell.border = Rectangle.NO_BORDER
            labelCell.paddingBottom = 3f
            contactsTable.addCell(labelCell)

            val valueCell = PdfPCell(Phrase(value, fontNormal))
            valueCell.border = Rectangle.NO_BORDER
            valueCell.paddingBottom = 3f
            contactsTable.addCell(valueCell)
        }

        addContactRow("Телефон:", "+7 (902) 050-40-50")
        addContactRow("Email:", "broker@alterna.ltd")
        addContactRow("Сайт:", "www.alternadv.com")

        rightCell.addElement(contactsTable)
        headerTable.addCell(rightCell)
        headerTable.setSpacingAfter(20f)

        // Добавляем всё в документ
        document.add(headerTable)

        //
        val p = Paragraph(title, fontBold)
        p.alignment = Element.ALIGN_CENTER
        document.add(p)
        //document.add(Chunk.NEWLINE)

        // ---- Таблица результатов ----
        if (resultTitle.isNotBlank()) {
            document.add(Paragraph(resultTitle, fontBold))
        }

        if (results.count() > 0) {
            val resultsTable = createTable(results, fontNormal, fontBold)
            document.add(resultsTable)
            document.add(Chunk.NEWLINE)
        }

        if (result2Title.isNotBlank()) {
            document.add(Paragraph(result2Title, fontBold))
        }

        if (results2.count() > 0) {
            val results2Table = createTable(results2, fontNormal, fontBold)
            document.add(results2Table)
            document.add(Chunk.NEWLINE)
        }

        // ---- Таблица параметров ----
        document.add(Paragraph("Параметры", fontBold))
        val paramsTable = createTable(parameters, fontNormal, fontBold, true)
        document.add(paramsTable)
        document.add(Chunk.NEWLINE)

        // ---- Дата ----
        val dated = getCurrentDateTimeRu()
        document.add(Paragraph("Дата расчета: $dated", fontNormal))
        document.add(Chunk.NEWLINE)

        // ---- Комментарий ----
        comments?.takeIf { it.isNotBlank() }?.let {
            document.add(Paragraph(it, fontNormal))
        }

        document.close()
        return outFile
    }

    private fun createTable(
        data: List<ReportRowModel>,
        fontNormal: Font,
        fontBold: Font,
        use2Cols: Boolean = false
    ): PdfPTable {
        val table = if (!use2Cols) PdfPTable(4) else PdfPTable(2)
        table.setSpacingBefore(10f)
        table.widthPercentage = 100f

        if (!use2Cols) {
            table.setWidths(floatArrayOf(1f, 2f, 1f, 1f))

            // Заголовки
            table.addCell(createCell("Вид", fontBold))
            table.addCell(createCell("Ставка", fontBold))
            table.addCell(createCell("Сумма, руб", fontBold))
            table.addCell(createCell("Сумма, $", fontBold))

        } else {
            table.setWidths(floatArrayOf(1f, 2f))
        }

        data.forEach {
            val o = if (it.bold) fontBold else fontNormal
            table.addCell(Phrase(it.first, o))
            table.addCell(Phrase(it.second, o))
            if (!use2Cols) {
                table.addCell(createCell(it.third, o, alignment = Element.ALIGN_RIGHT))
                table.addCell(createCell(it.fourth, o, alignment = Element.ALIGN_RIGHT))
            }
        }

        return table
    }

    private fun createCell(
        text: String,
        font: Font,
        alignment: Int = Element.ALIGN_CENTER
    ): PdfPCell {
        val cell = PdfPCell(Phrase(text, font))
        cell.horizontalAlignment = alignment
        return cell
    }
*/
}

fun buildReportRows(calcResult: CalcResultModel?): Pair<List<ReportRowModel>, List<ReportRowModel>> {
    val parameters = mutableListOf<ReportRowModel>()
    val results = mutableListOf<ReportRowModel>()

    if (calcResult == null) {
        return parameters to results
    }

    calcResult.chosen?.let { chosen ->
        parameters.add(ReportRowModel("Код ТН ВЭД", chosen.code))
        parameters.add(ReportRowModel("Направление", chosen.direction))
        parameters.add(ReportRowModel("Страна", chosen.country))
        chosen.paramCost?.let {
            parameters.add(ReportRowModel("Стоимость (USD)", String.format(Locale.US, "%,.2f", it)))
        }
        chosen.addons?.forEach { (k, v) ->
            parameters.add(ReportRowModel("Параметр $k", String.format(Locale.US, "%,.2f", v)))
        }
        chosen.specials?.forEach { (k, v) ->
            parameters.add(ReportRowModel("Особое условие $k", v))
        }
    }

    calcResult.calculation?.let { calc ->
        calc.payments?.forEach { payment ->
            results.add(
                ReportRowModel(
                    payment.name ?: "",
                    payment.rate ?: "",
                    third = String.format(Locale.US, "%,.2f", payment.summaRub ?: 0.0),
                    fourth = String.format(Locale.US, "%,.2f", payment.summaUsd ?: 0.0)
                )
            )
        }

        results.add(
            ReportRowModel(
                "Итого",
                second = "",
                third = String.format(Locale.US, "%,.2f", calc.costRub ?: 0.0),
                fourth = String.format(Locale.US, "%,.2f", calc.costUsd ?: 0.0),
                bold = true
            )
        )
    }

    return parameters to results
}

fun buildCarReportRows(calcResult: CarCalcResultModel?): CarReportRows {
    val parameters = mutableListOf<ReportRowModel>()
    val resultsF = mutableListOf<ReportRowModel>()
    val resultsU = mutableListOf<ReportRowModel>()

    if (calcResult == null) {
        return CarReportRows(parameters, resultsF, resultsU)
    }

    val months = CarCalcState()
    val month = months.months[calcResult.chosen.month?.toInt()]

    val vehicle = VehicleTypes[calcResult.chosen.vehicle]

    // ---- Параметры авто ----
    calcResult.chosen.let { chosen ->
        parameters.add(ReportRowModel("ТС", vehicle ?: ""))
        parameters.add(ReportRowModel("Дата выпуска", "${month ?: ""} ${chosen.year ?: ""}"))
        parameters.add(ReportRowModel("Стоимость, дол. США", chosen.cost ?: ""))
        parameters.add(ReportRowModel("Объем двигателя, куб.см.", chosen.capacity ?: ""))
        parameters.add(ReportRowModel("Мощность, л.с.", chosen.power ?: ""))
        parameters.add(ReportRowModel("Количество мест", chosen.seats ?: ""))
        parameters.add(ReportRowModel("Масса, кг.", chosen.weight ?: ""))
        parameters.add(ReportRowModel("Объем багажника, куб.см.", chosen.bag ?: ""))
    }

    // ---- Результаты F ----
    calcResult.calculation.f?.let { fCalc ->
        fCalc.payments?.forEach { payment ->
            resultsF.add(
                ReportRowModel(
                    first = payment.name,
                    second = payment.rate,
                    third = String.format(Locale.US, "%,.2f", payment.sumRub),
                    fourth = String.format(Locale.US, "%,.2f", payment.sumUsd)
                )
            )
        }
        resultsF.add(
            ReportRowModel(
                first = "Итого",
                second = "",
                third = String.format(Locale.US, "%,.2f", fCalc.paymentsSumRub ?: 0.0),
                fourth = String.format(Locale.US, "%,.2f", fCalc.paymentsSumUsd ?: 0.0),
                bold = true
            )
        )
    }

    // ---- Результаты U ----
    calcResult.calculation.u?.let { uCalc ->
        uCalc.payments?.forEach { payment ->
            resultsU.add(
                ReportRowModel(
                    first = payment.name,
                    second = payment.rate,
                    third = String.format(Locale.US, "%,.2f", payment.sumRub),
                    fourth = String.format(Locale.US, "%,.2f", payment.sumUsd)
                )
            )
        }
        resultsU.add(
            ReportRowModel(
                first = "Итого",
                second = "",
                third = String.format(Locale.US, "%,.2f", uCalc.paymentsSumRub ?: 0.0),
                fourth = String.format(Locale.US, "%,.2f", uCalc.paymentsSumUsd ?: 0.0),
                bold = true
            )
        )
        parameters.add(ReportRowModel("Код ТНВЭД", uCalc.tnved ?: ""))
    }

    return CarReportRows(parameters, resultsF, resultsU)
}

fun getCurrentDateTimeRu(): String {
    val locale = Locale.forLanguageTag("ru-RU")   // <-- новый способ
    val sdf = SimpleDateFormat("dd MMMM yyyy HH:mm", locale)
    return sdf.format(Date())
}