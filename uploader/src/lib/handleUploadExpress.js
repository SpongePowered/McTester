const handleUpload = require('./handleUpload.js');

module.exports = function(req, res) {
    handleUpload(req).then(code => {
        res.sendStatus(code);
    }).catch(bad => {
        res.status(400).send(bad);
    });
}
