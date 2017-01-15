package de.tu_berlin.formic.example

import java.io.File

import akka.actor.ActorSystem
import org.apache.commons.io.FileUtils

import scala.util.Try

/**
  * @author Ronny Bräunlich
  */
trait PersistenceCleanup {

  def deleteStorageLocations(system: ActorSystem): Unit = {
    val storageLocations = List(
      "akka.persistence.journal.leveldb.dir",
      "akka.persistence.journal.leveldb-shared.store.dir",
      "akka.persistence.snapshot-store.local.dir").map { s =>
      new File(system.settings.config.getString(s))
    }
    storageLocations.foreach(dir => Try(FileUtils.deleteDirectory(dir)))
  }
}