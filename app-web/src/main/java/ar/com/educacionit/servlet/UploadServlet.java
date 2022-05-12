package ar.com.educacionit.servlet;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Collection;

import ar.com.educacionit.domain.Articulos;
import ar.com.educacionit.generic.CSVFileParser;
import ar.com.educacionit.generic.IParser;
import ar.com.educacionit.generic.ParseException;
import ar.com.educacionit.generic.XLSXFileParser;
import ar.com.educacionit.web.enums.Enumerable;
import ar.com.educacionit.web.enums.ViewEnums;
import ar.com.educacionit.web.enums.ViewKeysEnum;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

@WebServlet("/controllers/UploadServlet")
@MultipartConfig
public class UploadServlet extends BaseServlet {

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
	
		Part filePart = req.getPart(ViewKeysEnum.UPLOAD_FILE.getParam());
		
		ViewEnums target = ViewEnums.UPLOAD_PREVIEW;
		
		//validamos 
		if(filePart == null | filePart.getSize() == 0) {
			target = ViewEnums.UPLOAD;
			addAttribute(req, ViewKeysEnum.ERROR_GENERAL, "Debe selecionar un archivo");
			redirect(target, req, resp);
		}
		
		String fileName = Paths.get(filePart.getSubmittedFileName()).getFileName().toString();
		
		String ext = this.getExt(fileName);
		
		IParser<Collection<Articulos>> parser;
		switch (ext) {
		case "csv":
			parser = new CSVFileParser(filePart);
			break;
		case "xls":
			parser = new XLSXFileParser(ext);
			break;				
		default:
			parser = null;
			break;
		}
		
		if(parser != null) {
			try {
				Collection<Articulos> articulos = parser.parse();
				super.addAttribute(req.getSession(), ViewKeysEnum.UPLOAD_PREVIEW_KEY, articulos);
			} catch (ParseException e) {
				super.addAttribute(req, ViewKeysEnum.ERROR_GENERAL, e.getMessage());
				target = ViewEnums.UPLOAD;
			}
		}else {
			target = ViewEnums.UPLOAD;
			super.addAttribute(req, ViewKeysEnum.ERROR_GENERAL, "Formato no soportado");
		}
		
		req.getSession().setAttribute(Enumerable.ENUMPARAM, ViewKeysEnum.UPLOAD_PREVIEW_KEY);
		super.redirect(target, req,resp);
		
		//me falta una redirect
		
	}

	private String getExt(String fileName) {
		String[] names = fileName.split("\\.");		
		return names[1];
	}
}
