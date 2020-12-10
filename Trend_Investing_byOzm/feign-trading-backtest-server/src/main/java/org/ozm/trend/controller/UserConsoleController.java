package org.ozm.trend.controller;


import org.ozm.trend.pojo.Example;
import org.ozm.trend.pojo.User;
import org.ozm.trend.service.ExampleService;
import org.ozm.trend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.ClassUtils;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

//我的工作台
@RestController
public class UserConsoleController {

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Autowired
    UserService userService;
    @Autowired
    ExampleService exampleService;

    public final static String UPLOAD_PATH_PREFIX = "static/uploadFile/";

    //上传数据
    @PostMapping("/userconsole")
    @CrossOrigin
    public Map<String, Object> consoleData(
            Example example,
            MultipartFile file
    ) throws FileNotFoundException {
        Map<String,Object> map = new HashMap<>();
        if (file.isEmpty()){
            map.put("msg","上传的文件有误");
            map.put("response","error");
            return map;
        }

        //获取当前缓存的user
        //1、将传入code和name保存在example上，并通过dao操作，将code和name保存在数据库example表上
        System.out.println(example.getCode()+example.getName());
        //2、通过获得的缓存，得到对应的user。将对应的user保存在example对象上
        String username=stringRedisTemplate.opsForValue().get("loginResponse");

        //校验是否存在相同的code如有则返回错误


        User user = userService.get(username);
        if (user!=null){
            example.setUser(user);
        }
        //3、添加创建时间 和设置状态，默认状态为未导入ready 导入状态为success
        long l = System.currentTimeMillis();
        Date time=new Date(l);
        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        example.setCreate_time(sdf.format(time));
        System.out.println(time);
        example.setStatus("ready");
        //4、将id、code、name、user保存在example对象上，并在数据库上更新
        exampleService.save(example);
        //5、文件保存至静态目录,以Example id作为文件名，读取文件内容
        //Ps:用id获得对应用户上传的文件内容本身是不安全的，最安全应该是以盐加密和解密
        upload(file,example,map);

        return map;
    }


    //读取实例
    @GetMapping("/readUserConsole")
   public Map<String,Object> readConsole(){
        Map<String,Object> map = new HashMap<>();
        List<Example> userlist = new ArrayList<>();
        //1、利用缓存读出用户的id，并通过example表上关联的uid读出对应用户的Example数据
        String username=stringRedisTemplate.opsForValue().get("loginResponse");
        User user = userService.get(username);
        if (user==null){
            map.put("msg","读取错误");
        }else{
            userlist = exampleService.get(user);
        }
        map.put("msg","读取成功");
        map.put("userExampleList",userlist);

        return map;

    }

    //导入实例
    @GetMapping("/importUserConsole")
    public Map<String,Object> importUser(@RequestParam("eid")int id){
        String username = stringRedisTemplate.opsForValue().get("loginResponse");
        //将对应user的example对应id实例中的status转为success
        Map<String,Object> map = new HashMap<>();
        User user = userService.get(username);

        if (user==null){
            map.put("msg","导入错误");
            map.put("response","error");
            return map;
        }

        Example example = exampleService.getI(user,id);
        if (example.getStatus().equals("success")){
            example.setStatus("ready");
            map.put("msg","导出成功");
        }else{
            example.setStatus("success");
            map.put("msg","导人成功");
        }
        exampleService.save(example);
        map.put("response","success");
        return  map;
    }

    //删除当前实例
    @GetMapping("/removeUserConsole/{eid}")
    public String remove(@PathVariable("eid")int id) throws FileNotFoundException {

        String username = stringRedisTemplate.opsForValue().get("loginResponse");
        User user = userService.get(username);

        if (user==null){
            return "删除失败，请重新登录后再次尝试";
        }else {
            Example example = exampleService.getI(user,id);
            //从数据库中删除字段
            exampleService.remove(example);
            //从文件流中删除文件
            String fileFolder = ResourceUtils.getURL("classpath:").getPath()+"public/uploadFile/"+user.getUsername();
            //文件名
            String fileName = "/"+example.getId()+".json";
            //读取json文件内容
            File f = new File(fileFolder+fileName);
            if (f.exists()){
                //删除对应文件
                f.delete();
            }
        }

        return "删除成功";
    }











    public void  upload(MultipartFile file,Example example, Map<String,Object> map) throws FileNotFoundException {
        //设置要上传的目录
        String fileFolder = ResourceUtils.getURL("classpath:").getPath()+"public/uploadFile/"+example.getUser().getUsername();
        //设置文件名
        String fileName = "/"+example.getId()+".json";
        //创建目标目录
        File folder = new File(fileFolder);
        if (!folder.exists()){
            folder.mkdirs();
        }
        //创建目标文件
        File  destFile= new File(folder+fileName);
        //将上传的文件复制到刚创建的目标文件上
        try{
            file.transferTo(destFile);
            map.put("msg","文件上传成功");
            map.put("response","success");
        }catch (IOException e){
            e.printStackTrace();
            map.put("msg","文件上传失败，请重新尝试");
            map.put("response","error");
        }
    }




    //上传文件保存至本地以及保存在数据库上  保留写法，用于拓展
    public void upload2(MultipartFile file,Example example, Map<String,Object> map){
        //在指定路径下创建一个指定名称的文件，并将json对应传入到本地和数据表中。
        //逻辑： ①在指定路径下创建一个指定名称的文件，②从上传文件的输入流中获取对应内容
        //       ③用创建好的指定名称的文件创建对应的输出流。用于接收上传文件的输入流上的内容
        //       ④最后一步使用FileCopyUtils.copy方法，将上传文件输入流的内容拷贝到指定路径文件的输出流上

        //创建输入输出流
        InputStream inputStream = null;
        OutputStream outputStream = null;

        try{
            //确定上传文件的位置 上传至第三方模块中
            String path = "src/main/resources/"+UPLOAD_PATH_PREFIX+example.getUser().getUsername()+"/";
            //获取文件上传的输出流
            inputStream = file.getInputStream();
            //获取上传时的文件名
            String ufileName = file.getOriginalFilename();
            System.out.println(ufileName);
            //设置保存在用户目录下文件名
            String fileName = example.getId()+".json";
            //将对应文件路径以及要上传后的文件名创建成文件对象
            File realfile = new File(path+fileName);
            //判断文件父目录是否存在
            if(!realfile.getParentFile().exists()){
                //不存在就创建一个
                realfile.getParentFile().mkdir();
            }
            //获取文件的输出流
            outputStream = new FileOutputStream(realfile);

            //最后使用资源访问器FileCopyUtils的copy方法拷贝文件
            FileCopyUtils.copy(inputStream, outputStream);

            System.out.println("文件上传成功");

        }catch (IOException e){
            e.printStackTrace();
            map.put("msg","上传文件时发生错误");
            map.put("response","error");
        }
    }




}
