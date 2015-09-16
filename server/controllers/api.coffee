express = require('express')
# todo: fix weird way of accesing file
path = require('path')
db = require(path.join(__dirname, 'utilities/DB'))

api = express.Router()

picturesTable = db.get('pictures')

api.post '/api', (req, res) ->
    newPicture = req.query.picture
    picturesTable.insert({ image: newPicture })
    res.json "yay"

api.get '/api', (req, res) ->
    picturesTable.find {}, (err, pictures) ->
        throw err if err
        res.json pictures


module.exports = api
