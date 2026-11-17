package org.example.royecttofinal.Model;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

public class ReciboPdfService {

    private static final Color COLOR_PRIMARIO = new Color(15, 23, 42);
    private static final Color COLOR_ACENTO = new Color(37, 99, 235);
    private static final Color COLOR_VERDE = new Color(22, 163, 74);
    private static final Color COLOR_FONDO_SUAVE = new Color(248, 250, 252);
    private static final Color COLOR_BORDE = new Color(203, 213, 225);

    public File generarReciboPdf(Pedido pedido, MetodoPago.ResultadoPago resultadoPago) {
        if (pedido == null || pedido.getComandaDigital() == null) {
            throw new IllegalStateException("El pedido debe estar confirmado y con comanda KDS antes de generar el recibo PDF.");
        }

        File directorioSalida = new File("recibos");
        if (!directorioSalida.exists()) {
            directorioSalida.mkdirs();
        }

        File archivoPdf = new File(directorioSalida, "Recibo_" + pedido.getIdPedido() + ".pdf");
        Document document = new Document(PageSize.LETTER, 36, 36, 36, 36);

        try {
            PdfWriter.getInstance(document, new FileOutputStream(archivoPdf));
            document.open();

            Font fontTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, COLOR_PRIMARIO);
            Font fontSubtitulo = FontFactory.getFont(FontFactory.HELVETICA, 9.5f, new Color(71, 85, 105));
            Font fontSeccion = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, COLOR_ACENTO);
            Font fontNegrita = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9.5f, Color.BLACK);
            Font fontNormal = FontFactory.getFont(FontFactory.HELVETICA, 9.5f, Color.BLACK);
            Font fontHeaderTabla = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9.5f, Color.WHITE);
            Font fontTotal = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, COLOR_PRIMARIO);
            Font fontVerde = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9.5f, COLOR_VERDE);

            DateTimeFormatter fmtFecha = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String fechaTexto = pedido.getFechaConfirmacion() != null
                    ? pedido.getFechaConfirmacion().format(fmtFecha)
                    : "N/A";

            Paragraph titulo = new Paragraph("RESTOSOFTWARE — COMPROBANTE DE PEDIDO Y COMANDA", fontTitulo);
            titulo.setSpacingAfter(2f);
            document.add(titulo);

            Paragraph sub = new Paragraph(
                    "Orden: " + pedido.getIdPedido()
                            + "  |  Fecha/Hora: " + fechaTexto
                            + "  |  Operador: " + (pedido.getOperadorAuditor() != null ? pedido.getOperadorAuditor().getEtiqueta() : "Sistema"),
                    fontSubtitulo
            );
            sub.setSpacingAfter(12f);
            document.add(sub);

            document.add(crearTituloSeccion("1. Datos del Cliente y Cobertura Geográfica (RF-01 / RN-02)", fontSeccion));
            PdfPTable tablaCliente = new PdfPTable(2);
            tablaCliente.setWidthPercentage(100);
            tablaCliente.setSpacingAfter(10f);

            Cliente cli = pedido.getCliente();
            String zonaNombre = pedido.getZonaAplicada() != null ? pedido.getZonaAplicada().getNombreZona() : "Autorizada";
            String tiempoEst = pedido.getZonaAplicada() != null ? pedido.getZonaAplicada().getTiempoEstimadoMinutos() + " min" : "30 min";

            tablaCliente.addCell(crearCeldaInfo("Cliente: " + (cli != null ? cli.getNombre() : "-"), fontNormal));
            tablaCliente.addCell(crearCeldaInfo("Teléfono: " + (cli != null ? cli.getTelefono() : "-"), fontNormal));
            tablaCliente.addCell(crearCeldaInfo("Dirección: " + (cli != null ? cli.getDireccion().toString() : "-"), fontNormal));
            tablaCliente.addCell(crearCeldaInfo("Cobertura: " + zonaNombre + " (Entrega est. " + tiempoEst + ")", fontVerde));
            document.add(tablaCliente);

            document.add(crearTituloSeccion("2. Detalle de Platos, Adicionales y Restricciones (RF-02 a RF-05)", fontSeccion));
            PdfPTable tablaItems = new PdfPTable(4);
            tablaItems.setWidthPercentage(100);
            tablaItems.setWidths(new float[]{0.8f, 3.6f, 3.3f, 1.3f});
            tablaItems.setSpacingAfter(10f);

            tablaItems.addCell(crearCeldaHeader("Cant.", fontHeaderTabla, Element.ALIGN_CENTER));
            tablaItems.addCell(crearCeldaHeader("Plato", fontHeaderTabla, Element.ALIGN_LEFT));
            tablaItems.addCell(crearCeldaHeader("Personalización y Alérgenos", fontHeaderTabla, Element.ALIGN_LEFT));
            tablaItems.addCell(crearCeldaHeader("Subtotal", fontHeaderTabla, Element.ALIGN_RIGHT));

            for (DetallePedido item : pedido.getItems()) {
                String mods = item.getModificadores().isEmpty()
                        ? "Sin adicionales"
                        : "Extras: " + item.getModificadores().stream().map(Modificador::getNombre).collect(Collectors.joining(", "));
                String aler = item.getAlergenos().isEmpty()
                        ? "Alérgenos: Ninguno"
                        : "Alérgenos: " + item.getAlergenos().stream().map(Alergeno::toString).collect(Collectors.joining(", "));
                String rest = "Restricción: " + item.getRestriccion().toString();
                String obs = item.getObservaciones().isBlank() ? "" : "\nObs: " + item.getObservaciones();

                tablaItems.addCell(crearCeldaDato(String.valueOf(item.getCantidad()), fontNegrita, Element.ALIGN_CENTER));
                tablaItems.addCell(crearCeldaDato(
                        item.getProducto().getNombre() + "\n($" + String.format("%.2f", item.getProducto().getPrecioBase()) + " c/u)",
                        fontNormal, Element.ALIGN_LEFT));
                tablaItems.addCell(crearCeldaDato(mods + "\n" + aler + " | " + rest + obs, fontNormal, Element.ALIGN_LEFT));
                tablaItems.addCell(crearCeldaDato(String.format("$%.2f", item.getSubtotal()), fontNegrita, Element.ALIGN_RIGHT));
            }
            document.add(tablaItems);

            document.add(crearTituloSeccion("3. Liquidación Financiera y Facturación (RF-06 a RF-09)", fontSeccion));
            PdfPTable tablaTotales = new PdfPTable(2);
            tablaTotales.setWidthPercentage(55);
            tablaTotales.setHorizontalAlignment(Element.ALIGN_RIGHT);
            tablaTotales.setSpacingAfter(12f);

            tablaTotales.addCell(crearCeldaDato("Subtotal Ítems:", fontNormal, Element.ALIGN_LEFT));
            tablaTotales.addCell(crearCeldaDato(String.format("$%.2f", pedido.getSubtotalItems()), fontNormal, Element.ALIGN_RIGHT));

            tablaTotales.addCell(crearCeldaDato("Costo de Envío (Flete):", fontNormal, Element.ALIGN_LEFT));
            tablaTotales.addCell(crearCeldaDato(String.format("$%.2f", pedido.getTarifaFlete()), fontNormal, Element.ALIGN_RIGHT));

            String textoCupon = pedido.getCuponAplicado() != null
                    ? "Descuento (" + pedido.getCuponAplicado().getCodigo() + "):"
                    : "Descuento Aplicado:";
            tablaTotales.addCell(crearCeldaDato(textoCupon, fontNormal, Element.ALIGN_LEFT));
            tablaTotales.addCell(crearCeldaDato(String.format("-$%.2f", pedido.getDescuento()), fontVerde, Element.ALIGN_RIGHT));

            PdfPCell lblTot = crearCeldaDato("MONTO TOTAL A PAGAR:", fontTotal, Element.ALIGN_LEFT);
            lblTot.setBackgroundColor(COLOR_FONDO_SUAVE);
            PdfPCell valTot = crearCeldaDato(String.format("$%.2f", pedido.getTotalFinal()), fontTotal, Element.ALIGN_RIGHT);
            valTot.setBackgroundColor(COLOR_FONDO_SUAVE);
            tablaTotales.addCell(lblTot);
            tablaTotales.addCell(valTot);
            document.add(tablaTotales);

            PdfPTable tablaPago = new PdfPTable(1);
            tablaPago.setWidthPercentage(100);
            tablaPago.setSpacingAfter(12f);

            String modalidad = pedido.getMetodoPago() != null ? pedido.getMetodoPago().getNombreModalidad() : "N/A";
            String refPago = resultadoPago != null ? resultadoPago.referenciaTransaccion : "N/A";
            String infoFactura;
            if (pedido.getFacturaElectronica() != null) {
                FacturaElectronica fe = pedido.getFacturaElectronica();
                infoFactura = "Factura Electrónica (RN-04): " + fe.getNumeroFactura() + "  |  CUFE: " + fe.getCufe();
            } else {
                infoFactura = "Factura Electrónica (RN-04): Pendiente de recaudo contra entrega (se emite al cobrar).";
            }

            PdfPCell celdaPago = new PdfPCell();
            celdaPago.setBackgroundColor(COLOR_FONDO_SUAVE);
            celdaPago.setBorderColor(COLOR_BORDE);
            celdaPago.setPadding(8f);
            celdaPago.addElement(new Paragraph("Modalidad de Pago: " + modalidad + "  |  Referencia: " + refPago, fontNegrita));
            celdaPago.addElement(new Paragraph(infoFactura, fontNormal));
            tablaPago.addCell(celdaPago);
            document.add(tablaPago);

            ComandaDigital cmd = pedido.getComandaDigital();
            document.add(crearTituloSeccion("4. Comanda Digital Transmitida a Cocina KDS (RF-10 / RN-06)", fontSeccion));
            PdfPTable tablaKds = new PdfPTable(1);
            tablaKds.setWidthPercentage(100);
            PdfPCell celdaKds = new PdfPCell();
            celdaKds.setBorderColor(COLOR_PRIMARIO);
            celdaKds.setPadding(8f);
            celdaKds.addElement(new Paragraph(
                    "ID Comanda: " + cmd.getIdComanda()
                            + "  |  Estado Orden: " + pedido.getEstado().getEtiqueta()
                            + "  |  Condición Financiera: " + cmd.getCondicionFinanciera().getEtiqueta(),
                    fontNegrita
            ));
            celdaKds.addElement(new Paragraph(
                    "Documento generado automáticamente por RestoSoftware (JavaFX MVC) con trazabilidad completa.",
                    fontSubtitulo
            ));
            tablaKds.addCell(celdaKds);
            document.add(tablaKds);

            document.close();
            return archivoPdf;

        } catch (DocumentException | IOException e) {
            throw new RuntimeException("No se pudo generar el recibo PDF del pedido: " + e.getMessage(), e);
        }
    }

    public File generarGuiaDespachoPdf(Pedido pedido) {
        if (pedido == null) {
            throw new IllegalArgumentException("Seleccione un pedido válido.");
        }
        File directorioSalida = new File("recibos");
        if (!directorioSalida.exists()) {
            directorioSalida.mkdirs();
        }
        File archivoPdf = new File(directorioSalida, "GuiaDespacho_" + pedido.getIdPedido() + ".pdf");
        Document document = new Document(PageSize.LETTER, 36, 36, 36, 36);

        try {
            PdfWriter.getInstance(document, new FileOutputStream(archivoPdf));
            document.open();

            Font fontTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, COLOR_PRIMARIO);
            Font fontSubtitulo = FontFactory.getFont(FontFactory.HELVETICA, 9.5f, new Color(71, 85, 105));
            Font fontSeccion = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, COLOR_ACENTO);
            Font fontNegrita = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9.5f, Color.BLACK);
            Font fontNormal = FontFactory.getFont(FontFactory.HELVETICA, 9.5f, Color.BLACK);
            Font fontHeaderTabla = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9.5f, Color.WHITE);

            DateTimeFormatter fmtFecha = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            Paragraph titulo = new Paragraph("RESTOSOFTWARE — GUÍA DE EMPAQUE, DESPACHO Y ENTREGA", fontTitulo);
            titulo.setSpacingAfter(2f);
            document.add(titulo);

            Paragraph sub = new Paragraph(
                    "Orden: " + pedido.getIdPedido()
                            + "  |  Emitida: " + LocalDateTime.now().format(fmtFecha)
                            + "  |  Estado: " + pedido.getEstado().getEtiqueta(),
                    fontSubtitulo
            );
            sub.setSpacingAfter(12f);
            document.add(sub);

            document.add(crearTituloSeccion("1. Destinatario y Condición de Recaudo", fontSeccion));
            PdfPTable tDest = new PdfPTable(2);
            tDest.setWidthPercentage(100);
            tDest.setSpacingAfter(10f);
            Cliente cli = pedido.getCliente();
            String condRecaudo = pedido.requiereRecaudoContraEntrega()
                    ? "COBRAR CONTRA ENTREGA: $" + String.format("%.2f", pedido.getTotalFinal())
                    : "PAGADO PREVIAMENTE ($0.00 por cobrar)";
            String repTxt = pedido.getRepartidorAsignado() != null
                    ? pedido.getRepartidorAsignado().toString()
                    : "Pendiente de asignar repartidor";

            tDest.addCell(crearCeldaInfo("Destinatario: " + (cli != null ? cli.getNombre() : "-"), fontNormal));
            tDest.addCell(crearCeldaInfo("Teléfono Contacto: " + (cli != null ? cli.getTelefono() : "-"), fontNormal));
            tDest.addCell(crearCeldaInfo("Dirección Entrega: " + (cli != null ? cli.getDireccion().toString() : "-"), fontNormal));
            tDest.addCell(crearCeldaInfo("Condición Financiera: " + condRecaudo, fontNegrita));
            tDest.addCell(crearCeldaInfo("Compromiso Entrega: " + pedido.getFechaCompromisoFormateada(), fontNormal));
            tDest.addCell(crearCeldaInfo("Repartidor Asignado: " + repTxt, fontNormal));
            document.add(tDest);

            document.add(crearTituloSeccion("2. Lista de Verificación de Empaque (Bodega / Cocina)", fontSeccion));
            PdfPTable tItems = new PdfPTable(3);
            tItems.setWidthPercentage(100);
            tItems.setWidths(new float[]{1.0f, 4.5f, 3.5f});
            tItems.setSpacingAfter(10f);
            tItems.addCell(crearCeldaHeader("Cant.", fontHeaderTabla, Element.ALIGN_CENTER));
            tItems.addCell(crearCeldaHeader("Producto Verificado", fontHeaderTabla, Element.ALIGN_LEFT));
            tItems.addCell(crearCeldaHeader("Especificaciones / Alérgenos", fontHeaderTabla, Element.ALIGN_LEFT));

            for (DetallePedido d : pedido.getItems()) {
                tItems.addCell(crearCeldaDato(String.valueOf(d.getCantidad()), fontNegrita, Element.ALIGN_CENTER));
                tItems.addCell(crearCeldaDato(d.getProducto().getNombre(), fontNormal, Element.ALIGN_LEFT));
                tItems.addCell(crearCeldaDato("Restricción: " + d.getRestriccion() + " | Obs: " + d.getObservaciones(), fontNormal, Element.ALIGN_LEFT));
            }
            document.add(tItems);

            document.add(crearTituloSeccion("3. Bitácora de Trazabilidad Operativa", fontSeccion));
            PdfPTable tLog = new PdfPTable(1);
            tLog.setWidthPercentage(100);
            PdfPCell cLog = new PdfPCell();
            cLog.setBackgroundColor(COLOR_FONDO_SUAVE);
            cLog.setBorderColor(COLOR_BORDE);
            cLog.setPadding(8f);
            for (String ev : pedido.getHistorialEventos()) {
                cLog.addElement(new Paragraph(ev, fontNormal));
            }
            tLog.addCell(cLog);
            document.add(tLog);

            document.close();
            return archivoPdf;
        } catch (DocumentException | IOException e) {
            throw new RuntimeException("Error generando guía de despacho PDF: " + e.getMessage(), e);
        }
    }

    private Paragraph crearTituloSeccion(String texto, Font font) {
        Paragraph p = new Paragraph(texto, font);
        p.setSpacingBefore(4f);
        p.setSpacingAfter(5f);
        return p;
    }

    private PdfPCell crearCeldaHeader(String texto, Font font, int alineacion) {
        PdfPCell cell = new PdfPCell(new Phrase(texto, font));
        cell.setBackgroundColor(COLOR_PRIMARIO);
        cell.setHorizontalAlignment(alineacion);
        cell.setPadding(6f);
        cell.setBorderColor(COLOR_PRIMARIO);
        return cell;
    }

    private PdfPCell crearCeldaDato(String texto, Font font, int alineacion) {
        PdfPCell cell = new PdfPCell(new Phrase(texto, font));
        cell.setHorizontalAlignment(alineacion);
        cell.setPadding(5f);
        cell.setBorderColor(COLOR_BORDE);
        return cell;
    }

    private PdfPCell crearCeldaInfo(String texto, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(texto, font));
        cell.setBackgroundColor(COLOR_FONDO_SUAVE);
        cell.setPadding(6f);
        cell.setBorderColor(COLOR_BORDE);
        return cell;
    }
}
