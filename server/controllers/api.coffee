fs = require('fs')
express = require('express')
mongoose = require('mongoose')
db = require('../Utilities/DB')


api = express.Router()

pictureSchema = new mongoose.Schema({
    image: Buffer
})
Picture = mongoose.model('picture', pictureSchema)


api.post '/api', (req, res) ->

    fileLocation = __dirname + '/../image.JPG'
    fileWriteStream = fs.createWriteStream(fileLocation)
    fileWriteStream.on 'finish', ->
        picture = new Picture
        picture.image = fs.readFileSync(fileLocation)
        fs.unlinkSync(fileLocation)
        picture.save (err, picture) ->
            throw err if err
        .then ->
            res.sendStatus(200)
    req.pipe(fileWriteStream)

api.get '/api', (req, res) ->
    Picture.find {}, (err, pictures) ->
        res.json pictures


module.exports = api
