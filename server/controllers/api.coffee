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
        ftp.put fullFilePath, './data/images/' + fileName + '.JPEG', (err) ->
            fs.unlink(fullFilePath)
            if err
                throw err
                res.sendStatus(500)
                return
            picture = new Picture
            picture.fileName = fileName
            picture.save((err, picture) ->
                if err
                    throw err
                    res.sendStatus(500)
                    return
                throw err
            ).then ->
                res.sendStatus(201)


api.get '/api', (req, res) ->
    currentLastFile = req.query.currentLastFile
    currentLastFile = -1 if not currentLastFile
    Picture.find({}).sort('fileName').exec (err, pictures) ->
        if err
            throw err
            res.sendStatus(500)
            return
        (
            if +picture.fileName > +currentLastFile
                ftp.get './data/images/' + picture.fileName + '.JPEG', (err, pictureFromFtp) ->
                    if err
                        throw err
                        res.sendStatus(500)
                        return
                    filePath = __dirname + '/' + picture.fileName + '.JPEG'
                    pictureFromFtp.pipe(fs.createWriteStream(filePath)).on 'finish', ->
                        res.set('fileName': picture.fileName)
                        res.sendFile filePath, ->
                            fs.unlink(filePath)
                return
        ) for picture in pictures
        res.sendStatus(404)

api.delete '/api/all', (req, res) ->
    Picture.find({}).sort('fileName').exec (err, pictures) ->
        if err
            throw err
            res.sendStatus(500)
            return
        (
            ftp.delete './data/images/' + picture.fileName + '.JPEG', (err) ->
                if err
                    throw err
                    res.sendStatus(500)
                    return
        ) for picture in pictures
        Picture.remove {}, (err) ->
            if err
                throw err
                res.sendStatus(500)
                return
        .then ->
            res.sendStatus(200)
        return

api.delete '/api', (req, res) ->
    fileName = req.query.fileName
    if not fileName
        res.sendStatus(500)
        return
    ftp.delete './data/images/' + fileName + '.JPEG', (err) ->
        if err
            throw err
            res.sendStatus(500)
            return
        Picture.remove {fileName: fileName}, (err) ->
            if err
                throw err
                res.sendStatus(500)
                return
        res.sendStatus(200)


module.exports = api
