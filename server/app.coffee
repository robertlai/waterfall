express = require('express')
logger = require('morgan')

router = require('./controllers/router')

app = express()

app.set('views', __dirname + '/views')
app.set('view engine', 'jade')
app.use(express.static('node_modules'))
app.use(express.static('server/public'))

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
