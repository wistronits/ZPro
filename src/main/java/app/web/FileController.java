package app.web;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import app.bean.FileMeta;
import app.services.SaeUpFileService;


@Controller
@RequestMapping("/upfile")
public class FileController {
	
	@Autowired
	private SaeUpFileService saeUpFileService;
	
	private LinkedList<FileMeta> files = new LinkedList<FileMeta>();
	private FileMeta fileMeta = null;
	
	@RequestMapping("/list")
	public String list(){
		return "upfile/list";
	}
	
	/**
	 * 文件上传功能
	 * MultipartHttpServletRequest 文件上传框架
	 */
	@RequestMapping(value="/upload", method = RequestMethod.POST)
	public @ResponseBody LinkedList<FileMeta> upload(MultipartHttpServletRequest request, HttpServletResponse response) {
 
		//获取上传文件集合中名称集合
		 Iterator<String> itr =  request.getFileNames();
		 MultipartFile mpf = null;

		 //遍例集合
		 while(itr.hasNext()){
			 
			 //通过request的getFile方法获取MultipartFile类型文件
			 mpf = request.getFile(itr.next()); 
			 
			 //files文件列表只装载10，大于10个，移除第一个
			 if(files.size() >= 10) files.pop();
			 
			 //创建文件
			 fileMeta = new FileMeta();
			 fileMeta.setFileName(mpf.getOriginalFilename());
			 fileMeta.setFileSize(mpf.getSize()/1024+" Kb");
			 fileMeta.setFileType(mpf.getContentType());
			 
			 try {
				 fileMeta.setBytes(mpf.getBytes());
				 saeUpFileService.upFile(mpf.getOriginalFilename(), mpf.getBytes());//保存
			} catch (IOException e) {
				e.printStackTrace();
			}
				
			 //记录处理
			 files.add(fileMeta);
			 
		 }
		 
		// 返回处理后结果，格式json
		// [{"fileName":"file.png","fileSize":"8 Kb","fileType":"image/png"},...]
		return files;
 
	}
	
	/**
	 * 获取文件
	 */
	@RequestMapping(value = "/get/{i}", method = RequestMethod.GET)
	public void get(HttpServletResponse response,@PathVariable Integer i){
		 FileMeta getFile = files.get(i);//List 使用序号获取文件
		 try {		
			 	response.setContentType(getFile.getFileType());
			 	response.setHeader("Content-disposition", "attachment; filename=\""+getFile.getFileName()+"\"");
		        //FileCopyUtils.copy(getFile.getBytes(), response.getOutputStream());//写出
			 	FileCopyUtils.copy(saeUpFileService.getFile(getFile.getFileName()), response.getOutputStream());//写出
		 }catch (IOException e) {
				e.printStackTrace();
		 }
	 }
	
}
