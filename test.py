import requests as rq
import xml.etree.ElementTree as ET

base_url = "http://localhost:8080/"
remote_url = "http://192.168.0.200:8080/"
default_app = "distributedGradeBook/"
gradebook_uri = "gradebook/"
secondary_uri = "secondary/"



def test_local_rest():
    """ Quick and dirty test 'suite' for comp655 project 2.
    Doesn't test every REST endpoint thoroughly, but does 
    test each one briefly.  Passing all tests doesn't imply 
    correct behavior in every case, but it does imply 
    correct behavior in at least some cases.
    """
    
    local_gradebook_url = base_url + default_app + gradebook_uri
    remote_secondary_url = remote_url + default_app + secondary_uri
    
    book_id = ""
    
# test PUT/POST /gradebook/{title}
#   create gradebook "book1"

    resp = rq.put(local_gradebook_url + "book1/")
    assert resp.status_code == 200

#test GET /gradebook AND store book_id of "book1" created in previous test
    resp = rq.get(local_gradebook_url)
    assert resp.status_code == 200
    
#   find and store book1's UUID
    xml_tree = ET.fromstring(resp.content.decode())
    for gradebook in xml_tree.findall('gradebook'):
        if gradebook.find('title').text == "book1":
            book_id = gradebook.find("id").text
    assert book_id != ""
    
#test PUT/POST /gradebook/{id}/student/{name}/grade/{grade}
    stu_name = "mike"
    stu_grade = "B"
    resp = rq.put(local_gradebook_url + book_id + "/student/" + stu_name + "/grade/" + stu_grade)
    assert resp.status_code == 200

#test GET /gradebook/{id}/student/{name}
    resp = rq.get(local_gradebook_url + book_id + "/student/" + stu_name)
    assert resp.status_code == 200
    assert stu_name in resp.content.decode()
    assert stu_grade in resp.content.decode()
    
#test GET /gradebook/{id}/student
#   this could be better by adding a few more students and making sure they are all present, and
#   by checking for correct xml

    resp = rq.get(local_gradebook_url + book_id + "/student/")
    assert resp.status_code == 200
    assert stu_name in resp.content.decode()
    assert stu_grade in resp.content.decode()

#test DELETE /gradebook/{id}/student/{name}
    resp = rq.delete(local_gradebook_url + book_id + "/student/" + stu_name)
    assert resp.status_code == 200
    resp = rq.get(local_gradebook_url + book_id + "/student/" + stu_name)
    assert resp.status_code == 404
    
#test delete /gradebook/{id}
    resp = rq.delete(local_gradebook_url + book_id)
    assert resp.status_code == 200
    resp = rq.get(local_gradebook_url)
    assert resp.status_code == 200
    assert book_id not in resp.content.decode()
    
#test secondary not yet implemented...
    
print("all tests passed")

if __name__ == "__main__":
    test_local_rest()