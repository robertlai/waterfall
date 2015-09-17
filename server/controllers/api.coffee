fs = require('fs')
express = require('express')
mongoose = require('mongoose')
db = require('../Utilities/DB')


api = express.Router()

pictureSchema = new mongoose.Schema({
    fileName: String
})
Picture = mongoose.model('picture', pictureSchema)


fileLocation = __dirname + '/../data/images/'

api.post '/api', (req, res) ->
    fileName = (new Date()).getTime()
    fileWriteStream = fs.createWriteStream(fileLocation + fileName + '.JPG')
    fileWriteStream.on 'finish', ->
        picture = new Picture
        picture.fileName = fileName
        picture.save (err, picture) ->
            throw err if err
        .then ->
            res.sendStatus(200)
    req.pipe(fileWriteStream)

api.get '/api', (req, res) ->
    currentLastFile = req.query.currentLastFile
    currentLastFile = -1 if not currentLastFile
    Picture.find({}).sort('fileName').exec (err, pictures) ->
        throw err if err
        (
            if +picture.fileName > +currentLastFile
                res.sendFile(picture.fileName + '.JPG', { root: fileLocation })
                return
        ) for picture in pictures
        res.sendStatus(404)


module.exports = api
