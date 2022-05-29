package com.imooc.controller.center;

import com.imooc.controller.BaseController;
import com.imooc.pojo.Users;
import com.imooc.pojo.bo.center.CenterUserBO;
import com.imooc.resource.FileUpload;
import com.imooc.service.center.CenterUserService;
import com.imooc.utils.CookieUtils;
import com.imooc.utils.DateUtil;
import com.imooc.utils.IMOOCJSONResult;
import com.imooc.utils.JsonUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Api(value = "用户中心-用户信息相关", tags = {"用户信息相关的api接口"})
@RestController
@RequestMapping("userInfo")
public class CenterUserController extends BaseController{
    @Autowired
    private CenterUserService centerUserService;

    @Autowired
    private FileUpload fileUpload;


    /**
     * 修改用户信息
     * @return 用户信息列表
     */
    @ApiOperation(value = "修改用户信息", notes = "修改用户信息", httpMethod = "POST")
    @PostMapping("/update")
    public IMOOCJSONResult update(
            @ApiParam(name = "userId", value = "用户ID", required = true)
            @RequestParam String userId,
            @ApiParam(name = "centerUserBO", value = "用户个人中心信息BO", required = true)
            @RequestBody @Valid CenterUserBO centerUserBO, // 此处valid是表示BO需要做字段验证
            BindingResult result, // 错误信息
            HttpServletRequest request, HttpServletResponse response){

        // 判断BindingResult是否保存错误的验证信息，如果有，则直接return
        if (result.hasErrors()){
            Map<String, String> errorMap = getErrors(result);
            // 错误集合
            return IMOOCJSONResult.errorMap(errorMap);
        }

        // 更新个人信息
        Users userResult = centerUserService.updateUserInfo(userId, centerUserBO);

        // 删除部分隐私信息
        userResult = setNullProperty(userResult);

        // 更新cookie信息
        CookieUtils.setCookie(request, response, BaseController.USER_COOKIE,
                JsonUtils.objectToJson(userResult), true);

        // TODO 后续要改，增加令牌token，会整合进redis，分布式会话

        return IMOOCJSONResult.ok();
    }

    // 将隐私信息删除，不显示
    private Users setNullProperty(Users userResult){
        userResult.setPassword(null);
        userResult.setMobile(null);
        userResult.setEmail(null);
        userResult.setCreatedTime(null);
        userResult.setUpdatedTime(null);
        userResult.setBirthday(null);

        return userResult;
    }

    /**
     * 获取centerUserBO中错误信息
     * @param result 校验结果
     * @return
     */
    private Map<String, String> getErrors(BindingResult result){

        Map<String, String> map = new HashMap<>();
        List<FieldError> errorList =  result.getFieldErrors();
        for (FieldError error: errorList){
            // 发生验证错误所对应的属性
            String errorField = error.getField();
            // 验证错误的信息
            String errorMsg = error.getDefaultMessage();

            map.put(errorField, errorMsg);
        }
        return map;
    }

    /**
     * 修改用户头像
     * @return
     */
    @ApiOperation(value = "修改用户头像", notes = "修改用户头像", httpMethod = "POST")
    @PostMapping("/uploadFace")
    public IMOOCJSONResult uploadFace(
            @ApiParam(name = "userId", value = "用户ID", required = true)
            @RequestParam String userId,
            @ApiParam(name = "file", value = "用户头像", required = true)
            MultipartFile file,
            HttpServletRequest request, HttpServletResponse response){

        // 定义头像保存的地址
        // String fileSpace = IMAGE_USER_FACE_LOCATION;
        // 通过配置文件获取地址
        String fileSpace = fileUpload.getImageUserFaceLocation();
        // 在路径上为每一个用户增加一个userid，用于区分不同用户上传
        String uploadPathPrefix = File.separator + userId;
        // 本地图片web映射地址
        String file2webPath = "/" + userId;

        // 开始文件上传
        if (file != null){
            FileOutputStream fileOutputStream = null;
            // 获得文件上传的文件名称
            try {
                String fileName = file.getOriginalFilename();

                if (!StringUtils.isBlank(fileName)){

                    /*
                        要将上传的用户名进行修改
                        命名格式为 face-{userid}.png
                        imooc-face.png -> ["imooc-face", "png"]
                        这是一种覆盖式的更新，即同时只能有一个头像文件
                        如果采用增量式的方式，可以在加入时间信息以保证所有的头像都能被保存
                     */

                    // 先将filename拆分
                    String[] fileNameArr = fileName.split("\\.");

                    // 获取文件的后缀名
                    String suffix = fileNameArr[fileNameArr.length - 1];

                    // 防止传入非图片的文件造成损害，需要判断文件后缀
                    if (!suffix.equalsIgnoreCase("png") &&
                        !suffix.equalsIgnoreCase("jpg") &&
                        !suffix.equalsIgnoreCase("jpeg")){
                        return IMOOCJSONResult.errorMsg("图片格式不正确");
                    }

                    // 文件名称重组
                    String newFileName = "face-" + userId + "." + suffix;

                    // 上传的头像最终保存的位置
                    String finalFacePath = fileSpace + uploadPathPrefix + File.separator + newFileName;

                    // 输出本地文件的保存路径
                    System.out.println("file path:" + finalFacePath);

                    // 用于提供给本地图片web映射
                    file2webPath += ("/" + newFileName);

                    File outFile = new File(finalFacePath);
                    // 如果没有父级目录
                    if (outFile.getParentFile() != null){
                        // 创建父级文件夹
                        outFile.getParentFile().mkdirs();
                    }

                    // 文件输出保存到目录
                    fileOutputStream = new FileOutputStream(outFile);
                    InputStream inputStream = file.getInputStream();
                    IOUtils.copy(inputStream, fileOutputStream);


                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (fileOutputStream != null){
                    try {
                        fileOutputStream.flush();
                        fileOutputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    System.out.println("无fileOutputStream");
                }
            }

        }else {
            return IMOOCJSONResult.errorMsg("文件不能为空");
        }

        // 获得本地图片在网站上的映射地址
        String imageServerUrl = fileUpload.getImageServerUrl();
        // 由于浏览器存在缓存的情况，所以在这里加上时间戳来保证更新后的图片可以及时刷新
        String finalUserFaceUrl = imageServerUrl + file2webPath
                + "?t=" + DateUtil.getCurrentDateString(DateUtil.DATE_PATTERN);
        // 更新用户头像到数据库
        Users userResult = centerUserService.updateUserFace(userId, finalUserFaceUrl);

        // 删除部分隐私信息
        userResult = setNullProperty(userResult);

        // 更新cookie信息
        CookieUtils.setCookie(request, response, BaseController.USER_COOKIE,
                JsonUtils.objectToJson(userResult), true);

        // TODO 后续要改，增加令牌token，会整合进redis，分布式会话

        return IMOOCJSONResult.ok();
    }
}
