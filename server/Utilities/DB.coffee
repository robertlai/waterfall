monk = require('monk')

db = monk('127.0.0.1:27017/waterfall')


module.exports = db
