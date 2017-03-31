package de.tu_berlin.formic.common.datastructure.client

import de.tu_berlin.formic.common.datastructure.FormicDataStructure

/**
  * @author Ronny Bräunlich
  */
trait DataStructureInitiator {

  def initDataStructure(dataType: FormicDataStructure)

}
