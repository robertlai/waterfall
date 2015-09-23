fs = require('fs')
express = require('express')
mongoose = require('mongoose')
db = require('../Utilities/DB')


api = express.Router()


pictureSchema = new mongoose.Schema({
    file: Buffer
    fileName: Number
})
Picture = mongoose.model('picture', pictureSchema)


api.post '/api', (req, res) ->
    fileName = (new Date()).getTime()
    fullFilePath = __dirname + '/' + fileName + Math.floor(Math.random() * 20000)

    req.pipe(fs.createWriteStream(fullFilePath)).on 'finish', ->
        picture = new Picture {
            fileName: fileName
            file: fs.readFileSync(fullFilePath)
        }
        picture.save (err, picture) ->
            if err
                res.sendStatus(500)
                throw err
            else
                res.sendStatus(201)
        .then ->
            fs.unlinkSync(fullFilePath)

api.get '/api', (req, res) ->
    lastFile = if req.query.lastFile then req.query.lastFile else -1

    Picture.find({}).sort('fileName').exec (err, files) ->
        if err
            res.sendStatus(500)
            throw err
        else
            (
                if file.fileName > lastFile
                    res.set('Content-Type': 'image/jpeg')
                    res.set('fileName': file.fileName)
                    res.send(file.file)
                    return
            ) for file in files
            res.sendStatus(404)

api.get '/api/list', (req, res) ->
    Picture.find({}).sort('fileName').exec (err, files) ->
        if err
            res.sendStatus(500)
            throw err
        else
            list = []
            list.push(file.fileName) for file in files
            res.json list

api.get '/api/fileName', (req, res) ->
    Picture.findOne({fileName: req.query.fileName}).exec (err, fileToSend) ->
        if err
            res.sendStatus(500)
            throw err
        else
            res.set('Content-Type': 'image/jpeg')
            res.send(fileToSend.file)

# todo: fix this (get list of images from ftp and delete them (based on containing jpeg and bing certain number of chars long))
# api.delete '/api/all', (req, res) ->
#     Picture.find({}).sort('fileName').exec (err, pictures) ->
#         if err
#             console.log err
#             res.sendStatus(500)
#         else
#             (
#                 ftp.delete './data/images/' + picture.fileName + '.JPEG', (err) ->
#                     if err
#                         console.log err
#                         res.sendStatus(500)
#             ) for picture in pictures
#             Picture.remove {}, (err) ->
#                 if err
#                     console.log err
#                     res.sendStatus(500)
#                 else
#                     res.sendStatus(200)

# api.delete '/api', (req, res) ->
#     fileName = req.query.fileName
#     if not fileName
#         res.sendStatus(500)
#         return
#     ftp.delete './data/images/' + fileName + '.JPEG', (err) ->
#         if err
#             console.log err
#             res.sendStatus(500)
#         else
#             Picture.remove {fileName: fileName}, (err) ->
#                 if err
#                     console.log err
#                     res.sendStatus(500)
#                 else
#                     res.sendStatus(200)


module.exports = api
