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
    fullFilePath = __dirname + '/' + fileName + '.JPG'

    fileWriteStream = fs.createWriteStream(fullFilePath)

    fileWriteStream.on 'finish', ->
        ftp.put fullFilePath, './data/images/' + fileName + '.JPG', (err) ->
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
    req.pipe(fileWriteStream)

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
                res.redirect('http://rcylai.ca/waterfall/data/images/' + picture.fileName + '.JPG')
                return
        ) for picture in pictures
        res.sendStatus(404)

# ftp.delete './data/images/' + fileName + '.JPG', (err) ->
#     console.log 'Error Deleting File From FTP!'
#     res.sendStatus(404)
#     return


module.exports = api
