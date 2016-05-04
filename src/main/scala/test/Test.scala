package test

import java.io.File

import scala.util.control.Breaks

/**
  * Created by C.J.YOU on 2016/1/19.
  */
  import java.awt.Color
  import java.awt.image.BufferedImage
  import java.io.{File, FileOutputStream}
  import java.util.{ArrayList, HashMap, List, Map}
  import javax.imageio.ImageIO

  import org.apache.commons.httpclient.{HttpClient, HttpStatus}
  import org.apache.commons.httpclient.methods.GetMethod
  import org.apache.commons.io.IOUtils
//
object Test {
    def isNotWhite(colorInt:Int):Int ={
      val color = new Color(colorInt)
      if (color.getRed + color.getGreen + color.getBlue > 100) {
        return 1
      }
      return 0
    }

    def isBlack(colorInt:Int) :Int = {
      val color = new Color(colorInt)
      if (color.getRed() + color.getGreen + color.getBlue() <= 100) {
        return 1
      }
      return 0
    }

    def removeBackgroud( picFile:String):BufferedImage = {
      val img = ImageIO.read(new File(picFile))
      val width = img.getWidth()
      val height = img.getHeight()
      println("org width :"+width)
      println("org height :"+height)
      for (x <- 0 until width) {
        for (y <- 0 until height) {
          if (isNotWhite(img.getRGB(x, y)) == 1) {
            img.setRGB(x, y, Color.WHITE.getRGB)
          } else {
            img.setRGB(x, y, Color.BLACK.getRGB)
          }
        }
      }
      return img
    }

    def  splitImage( img:BufferedImage): List[BufferedImage] = {
      val subImgs = new ArrayList[BufferedImage]()
      subImgs.add(img.getSubimage(0, 0, 23, 27))
      subImgs.add(img.getSubimage(23, 0, 23, 27))
      subImgs.add(img.getSubimage(46, 0, 23, 27))
      subImgs.add(img.getSubimage(69, 0, 23, 27))
      return subImgs
    }

    def  loadTrainData:Map[BufferedImage, String] = {
      val map = new HashMap[BufferedImage, String]()
      val dir = new File("H:\\SmartData-X\\算法\\验证码\\train")
      val files = dir.listFiles()
      for (index <- 0 until files.size) {
        val file = files(index)
        // println(file.getName.charAt(0) + "")
        map.put(ImageIO.read(file), file.getName.charAt(0) + "")
      }
      return map
    }

    def  getSingleCharOcr(img:BufferedImage, map:Map[BufferedImage, String]):String = {
      var result = ""
      val width = img.getWidth()
      val height = img.getHeight()
      var min = width * height
      val keySet = map.keySet().toArray
      for (index <- 0 until keySet.size) {
        val bi = keySet(index).asInstanceOf[BufferedImage]
        var count = 0
        val loop = new Breaks
        loop.breakable{
          for (x <- 0 until width) {
            for (y <- 0 until height){
              if (isNotWhite(img.getRGB(x, y)) != isNotWhite(bi.getRGB(x, y))) {
                count = count + 1
                if (count >= min)
                 loop.break()
              }
            }
          }
        }
        if (count < min) {
          min = count
          result = map.get(bi)
        }
      }
      return result
    }

    def  getAllOcr( file:String):String ={
      val img = removeBackgroud(file)
      // val listImg = splitImage(img)
      val map = loadTrainData
      var result = ""
     /* for (index <- 0 until listImg.size){
        val bi = listImg.get(index)
        result += getSingleCharOcr(bi, map)
      }*/
      result = getSingleCharOcr(ImageIO.read(new File("H:\\SmartData-X\\算法\\验证码\\train\\7.jpg")),map)
      ImageIO.write(img, "JPG", new File("H:\\SmartData-X\\算法\\验证码\\"+result+".jpg"))
      return result
    }

    def  downloadImage():Unit = {
      val httpClient = new HttpClient()
      val getMethod = new GetMethod(
        "http://www.puke888.com/authimg.php")
      for (i <- 0 to 30) {
        try {
          // 执行getMethod
          val statusCode = httpClient.executeMethod(getMethod)
          if (statusCode != HttpStatus.SC_OK) {
            System.err.println("Method failed: "
              + getMethod.getStatusLine)
          }
          // 读取内容
          val picName = "img//" + i + ".jpg"
          val inputStream = getMethod.getResponseBodyAsStream
          val outStream = new FileOutputStream(picName)
          IOUtils.copy(inputStream, outStream)
          outStream.close()
          System.out.println("OK!")
        } catch  {
          case e:Exception => e.printStackTrace()
        } finally {
          // 释放连接
          getMethod.releaseConnection()
        }
      }
    }

    /**
      * @param args
      * @throws Exception
      */
    def main(args:Array[String]):Unit ={
        val text = getAllOcr("H:\\SmartData-X\\算法\\验证码\\train\\0.jpg")
        System.out.println(".jpg = " + text)
    }
}
