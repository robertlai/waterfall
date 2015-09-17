express = require('express')
logger = require('morgan')

path = require('path')

router = require('./controllers/router')

app = express()

app.set('views', path.join(__dirname, 'views'))
app.set('view engine', 'jade')
app.use(express.static(__dirname + '/data/images'))

app.use logger('dev')
app.use(router)


if app.get('env') == 'development'
    app.use (err, req, res, next) ->
        res.status err.status or 500
        res.render('error', {
            message: err.message
            error: err
        })


app.use (err, req, res, next) ->
    res.status err.status or 500
    res.render('error', {
        message: err.message
        error: {}
    })


module.exports = app
