
/**
 * 获取时间戳精确到秒
 * @returns 
 */
export const getTimeStamp = () => {
  return Date.parse(new Date()) / 1000;
}

export const getDateFormat = (str = '-') => {
  // 获取当前时间
  let toDay = new Date();
  let year = toDay.getFullYear(); // 年
  let month = toDay.getMonth() + 1 < 10 ? '0' + (toDay.getMonth() + 1) : toDay.getMonth() + 1; // 月
  let data = toDay.getDate() < 10 ? '0' + toDay.getDate() : toDay.getDate(); // 日
  // let hour = toDay.getHours() < 10 ? '0' + toDay.getHours() : toDay.getHours(); // 时
  // let minute = toDay.getMinutes() < 10 ? '0' + toDay.getMinutes() : toDay.getMinutes(); // 分
  // let second = toDay.getSeconds() < 10 ? '0' + toDay.getSeconds() : toDay.getSeconds(); // 秒
  // let today = year + '-' + month + '-' + data + ' ' + hour + ':' + minute + ':' + second;
  let today = year + str + month + str + data;
  return today;
};



//将base64转换为文件对象
export const dataURLtoFile = (dataurl, filename) => {
  var arr = dataurl.split(',');
  var mime = arr[0].match(/:(.*?);/)[1];
  var bstr = atob(arr[1]);
  var n = bstr.length;
  var u8arr = new Uint8Array(n);
  while (n--) {
    u8arr[n] = bstr.charCodeAt(n);
  }
  //转换成file对象
  return new File([u8arr], filename, { type: mime });
  //转换成成blob对象
  //return new Blob([u8arr],{type:mime});
}



/**
 * 随机数
 * @param {} len 
 * @returns 
 */
export const randomString = (len) => {
  try {
    len = len || 32;
    // var $chars = '123456789';    /****默认去掉了容易混淆的字符oOLl,9gq,Vv,Uu,I1****/
    var $chars = 'ABCDEFGHJKMNPQRSTWXYZabcdefhijkmnprstwxyz2345678';    /****默认去掉了容易混淆的字符oOLl,9gq,Vv,Uu,I1****/
    var maxPos = $chars.length;
    var pwd = '';
    for (let i = 0; i < len; i++) {
      pwd += $chars.charAt(Math.floor(Math.random() * maxPos));
    }
    return pwd;
  } catch (err) {
    console.log(err)
  }
}