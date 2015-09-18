fs = require('fs')
express = require('express')
mongoose = require('mongoose')
db = require('../Utilities/DB')
FTP = require('ftp')


api = express.Router()


ftp = new FTP
ftp.connect({
  host: 'ftp.rcylai.ca'
  port: 21
  user: 'waterfall@rcylai.ca'
  password: 'Waterfall0pw'
})


pictureSchema = new mongoose.Schema({
    fileName: String
})
Picture = mongoose.model('picture', pictureSchema)


api.post '/api', (req, res) ->
    fileName = (new Date()).getTime()
    fullFilePath = __dirname + '/' + fileName + '.JPEG'

    req.pipe(fs.createWriteStream(fullFilePath)).on 'finish', ->
        fileBuffer = fs.readFileSync(fullFilePath)
        ftp.put fileBuffer, './data/images/' + fileName + '.JPEG', (err) ->
            fs.unlinkSync(fullFilePath)
            if err
                console.log 'Error Saving File To FTP!'
                res.sendStatus(404)
                return
            else
                console.log 'File transferred successfully!'
                picture = new Picture
                picture.fileName = fileName
                picture.save((err, picture) ->
                    if err
                        console.log 'Error Saving Image Name To Database!'
                        res.sendStatus(404)
                        return
                    console.log ''
                ).then ->
                    res.sendStatus(200)


api.get '/api', (req, res) ->
    currentLastFile = req.query.currentLastFile
    currentLastFile = -1 if not currentLastFile
    Picture.find({}).sort('fileName').exec (err, pictures) ->
        if err
            console.log 'Error Getting File Name From Database!'
            res.sendStatus(404)
            return
        (
            if +picture.fileName > +currentLastFile
                res.redirect('http://rcylai.ca/waterfall/data/images/' + picture.fileName + '.JPEG')
                return
        ) for picture in pictures
        res.sendStatus(404)

# ftp.delete './data/images/' + fileName + '.JPEG', (err) ->
#     console.log 'Error Deleting File From FTP!'
#     res.sendStatus(404)
#     return


module.exports = api
