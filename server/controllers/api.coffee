fs = require('fs')
express = require('express')
mongoose = require('mongoose')
db = require('../Utilities/DB')










JSFtp = require("jsftp")

ftp = new JSFtp({
  host: "ftp.rcylai.ca"
  port: 21
  user: "waterfall@rcylai.ca"
  pass: "Waterfall0pw"
})


api = express.Router()

pictureSchema = new mongoose.Schema({
    fileName: String
})
Picture = mongoose.model('picture', pictureSchema)


fileLocation = __dirname + '/../data/images/'

api.post '/api', (req, res) ->
    fileName = (new Date()).getTime()
    fullFilePath = fileLocation + fileName + '.JPG'

    fileWriteStream = fs.createWriteStream(fullFilePath)


    fileWriteStream.on 'finish', ->
        fileBuffer = fs.readFileSync(fullFilePath)
        ftp.put fileBuffer, './data/images/' + fileName + '.JPG', (err) ->
            if err
                throw err
                res.sendStatus(404)
            else
                console.log 'File transferred successfully!'
                picture = new Picture
                picture.fileName = fileName
                picture.save((err, picture) ->
                    throw err if err
                ).then ->
                    res.sendStatus(200)
    req.pipe(fileWriteStream)

api.get '/api', (req, res) ->
    currentLastFile = req.query.currentLastFile
    currentLastFile = -1 if not currentLastFile
    Picture.find({}).sort('fileName').exec (err, pictures) ->
        throw err if err
        (
            if +picture.fileName > +currentLastFile
                res.redirect('http://rcylai.ca/waterfall/data/images/' + picture.fileName + '.JPG')
                # res.sendFile(picture.fileName + '.JPG', { root: fileLocation })
                return
        ) for picture in pictures
        res.sendStatus(404)


module.exports = api
