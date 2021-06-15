const functions = require("firebase-functions");

const admin = require("firebase-admin");

admin.initializeApp();

const database = admin.firestore();

const dbRef = database.collection("users");

exports.timerUpdate = functions.pubsub.schedule("0 0 * * *")
    .onRun((context) => {
      const allUsers = dbRef.get()
          .then((snapshot) => {
            snapshot.forEach((doc) => {
              database.doc("users/" + doc.id)
                  .update({"status": "Pending..."});
            });
          })
          .catch((err) => {
            console.log("Error getting documents", err);
          });
      return allUsers;
    });
